@file:OptIn(ExperimentalJsExport::class, InternalJaktaAPI::class, DelicateCoroutinesApi::class)

package it.unibo.jakta.js

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.plan.BeliefAdditionPlan
import it.unibo.jakta.plan.BeliefRemovalPlan
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.GoalFailurePlan
import it.unibo.jakta.plan.GoalRemovalPlan
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.PlanScope
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.js.Promise
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.promise
import kotlinx.coroutines.withContext

/**
 * A trigger: given the triggering belief or goal, returns a context object if the plan is relevant,
 * `null`/`undefined` otherwise.
 */
typealias JsTrigger = (Any) -> Any?

/**
 * A guard: given the agent beliefs and the trigger context, returns a (possibly refined) context
 * if the plan is applicable, `null`/`undefined` otherwise.
 */
typealias JsGuard = (JsGuardScope) -> Any?

/**
 * A plan body: a (possibly `async`) function receiving the [JsPlanScope]. Its (awaited) return value
 * is the plan result, returned to whoever `achieve`d the goal.
 */
typealias JsPlanBody = (JsPlanScope) -> Any?

/**
 * Creates a new agent builder, optionally named [name].
 */
@JsExport
fun agent(name: String? = null): JsAgent = JsAgent(name)

/**
 * Sets the minimum logged severity: one of `Verbose`, `Debug`, `Info`, `Warn`, `Error`, `Assert`.
 * Agents' `print` logs at `Assert`, so `setLogSeverity("Assert")` shows only agent output.
 */
@JsExport
fun setLogSeverity(severity: String) = Logger.setMinSeverity(Severity.valueOf(severity))

// Kotlin varargs are exported as a single array parameter, not as JS rest parameters: take arrays explicitly.

/**
 * Runs the given [agents] on a single local node, resolving when the node terminates.
 */
@JsExport
fun runMas(agents: Array<JsAgentDefinition>): Promise<Unit> = GlobalScope.promise {
    mas(NodeBuilders.baseNode<Any>()) {
        node { withAgents(*agents.map { it.toJsAgent().toSpecification() }.toTypedArray()) }
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()) { JsAgentLifecycle(BaseAgentLifecycle(it)) })
}

/**
 * An agent that [runMas] can run: a [JsAgent], or an agent built by the facade of an incarnation (e.g., Prolog).
 */
@JsExport
abstract class JsAgentDefinition {
    /** The [JsAgent] this agent is built on. */
    @JsExport.Ignore
    @InternalJaktaAPI
    abstract fun toJsAgent(): JsAgent
}

/**
 * JS-friendly fluent builder for an agent whose beliefs and goals are arbitrary JS values
 * (compared with Kotlin equality, i.e. `===` for plain JS objects).
 */
@JsExport
class JsAgent internal constructor(private val name: String?) : JsAgentDefinition() {
    private val setup = mutableListOf<AgentBuilder<Any, Any, Any>.() -> Unit>()

    /** Adds [beliefs] to the initial beliefs. */
    fun believes(beliefs: Array<Any>): JsAgent = apply { setup += { beliefs.forEach(::addBelief) } }

    /** Adds [goals] to the initial goals. */
    fun hasInitialGoals(goals: Array<Any>): JsAgent = apply { setup += { goals.forEach(::addGoal) } }

    /** Adds a plan triggered by a belief addition. */
    fun onBeliefAdded(trigger: JsTrigger, body: JsPlanBody, guard: JsGuard? = null): JsAgent = apply {
        setup += { addBeliefPlan(BeliefAdditionPlan(trigger, guard.toKotlin(), body.toKotlin(node), anyResult)) }
    }

    /** Adds a plan triggered by a belief removal. */
    fun onBeliefRemoved(trigger: JsTrigger, body: JsPlanBody, guard: JsGuard? = null): JsAgent = apply {
        setup += { addBeliefPlan(BeliefRemovalPlan(trigger, guard.toKotlin(), body.toKotlin(node), anyResult)) }
    }

    /** Adds a plan triggered by a goal addition. */
    fun onGoalAdded(trigger: JsTrigger, body: JsPlanBody, guard: JsGuard? = null): JsAgent = apply {
        setup += { addGoalPlan(GoalAdditionPlan(trigger, guard.toKotlin(), body.toKotlin(node), anyResult)) }
    }

    /** Adds a plan triggered by a goal removal. */
    fun onGoalRemoved(trigger: JsTrigger, body: JsPlanBody, guard: JsGuard? = null): JsAgent = apply {
        setup += { addGoalPlan(GoalRemovalPlan(trigger, guard.toKotlin(), body.toKotlin(node), anyResult)) }
    }

    /** Adds a plan triggered by a goal failure. */
    fun onGoalFailed(trigger: JsTrigger, body: JsPlanBody, guard: JsGuard? = null): JsAgent = apply {
        setup += { addGoalPlan(GoalFailurePlan(trigger, guard.toKotlin(), body.toKotlin(node), anyResult)) }
    }

    /**
     * Adds [block] to the agent setup, for facades of incarnations (e.g., Prolog) that build their own plans
     * with [jsPlanBody].
     */
    @JsExport.Ignore
    @InternalJaktaAPI
    fun configure(block: AgentBuilder<Any, Any, Any>.() -> Unit): JsAgent = apply { setup += block }

    @JsExport.Ignore
    override fun toJsAgent(): JsAgent = this

    internal fun toSpecification(): (Node<Any>) -> AgentSpecification<Any, Any, Any> = agent(BaseAgentID(name)) {
        embodiedAs { Any() }
        setup.forEach { it() }
    }
}

/**
 * The scope available to a guard.
 */
@JsExport
class JsGuardScope internal constructor(
    /** The agent beliefs. */
    val beliefs: Array<Any>,
    /** The context returned by the trigger. */
    val context: Any,
)

/**
 * The scope available to a plan body.
 */
@JsExport
class JsPlanScope internal constructor(
    private val agent: MutableAgentState<Any, Any>,
    private val node: Node<Any>,
    private val intention: CoroutineScope,
    /** The context returned by the trigger (and guard). */
    val context: Any,
) {
    /** A snapshot of the agent beliefs. */
    val beliefs: Array<Any> get() = agent.beliefs.toTypedArray()

    /** The agent display name. */
    val agentName: String get() = agent.id.displayName

    /** Adds a belief. */
    fun believe(belief: Any) = agent.believe(belief)

    /** Removes a belief. */
    fun forget(belief: Any) = agent.forget(belief)

    /** Logs a message on the agent output. */
    fun print(message: String) = agent.print(message)

    /** Pursues [goal] as a subgoal, resolving with the result of the plan that achieved it. */
    fun achieve(goal: Any): Promise<Any?> = inIntention { agent.internalAchieve(goal, typeOf<Any?>()) }

    /** Suspends the plan for [millis] milliseconds, like Kotlin's `delay`, letting other intentions run. */
    fun delay(millis: Int): Promise<Unit> = inIntention { kotlinx.coroutines.delay(millis.toLong()) }

    /**
     * Waits for a promise not created by JaKtA (e.g., `fetch`) as a suspension of the plan: the code after
     * `await self.external(p)` runs within a step of the agent, and never runs if the plan is stopped meanwhile.
     */
    fun external(promise: Promise<Any?>): Promise<Any?> = inIntention { promise.await() }

    /** Pursues [goal] in a new intention, without waiting for it. */
    fun alsoAchieve(goal: Any) = agent.alsoAchieve(goal)

    /** Terminates the node the agent is running on. */
    fun terminateNode() = node.terminateNode()

    /**
     * Runs [block] in the plan's intention, started undispatched so it begins within the current step
     * (as Kotlin's suspending calls do). The returned promise settles within a later step of the intention,
     * and the [JsAgentLifecycle] lets the JS code it resumes run before the agent's next event.
     */
    private fun <T> inIntention(block: suspend () -> T): Promise<T> =
        // A supervisor child: a failure (e.g. of a subgoal) rejects only this promise, for JS to catch,
        // instead of cancelling the whole intention; cancelling the intention still cancels it.
        CoroutineScope(intention.coroutineContext + SupervisorJob(intention.coroutineContext.job))
            .promise(start = CoroutineStart.UNDISPATCHED) {
                try {
                    block()
                } finally {
                    intention.coroutineContext[JsSteps]?.resumedJs = true
                }
            }
}

/**
 * Runs an agent like [base], but when a step settles a promise of a [JsPlanScope], waits for the JS code awaiting it
 * before the next event: that code is the rest of the step, which the JS engine runs only after the step returns.
 */
private class JsAgentLifecycle<Belief : Any, Goal : Any>(private val base: AgentLifecycle<Belief, Goal>) :
    AgentLifecycle<Belief, Goal> by base {
    // One instance for the whole agent: plans keep the context of the step that launched them.
    private val steps = JsSteps()

    override suspend fun step() {
        withContext(steps) { base.step() }
        if (steps.resumedJs) {
            steps.resumedJs = false
            // A macrotask runs after all queued microtasks, i.e. after the resumed code reached its next `await`.
            Promise { resolve, _ -> js("setTimeout")({ resolve(Unit) }, 0) }.await()
        }
    }
}

private class JsSteps : AbstractCoroutineContextElement(JsSteps) {
    var resumedJs = false

    companion object Key : CoroutineContext.Key<JsSteps>
}

// JS plans are untyped: declaring Unit keeps them relevant both for `achieve` (Unit) and JS `achieve` (Any?).
private val anyResult = typeOf<Unit>()

private fun JsGuard?.toKotlin(): GuardScope<Any, Any>.() -> Any? =
    this?.let { guard -> { guard(JsGuardScope(beliefs.toTypedArray(), context)) } } ?: { context }

private fun JsPlanBody.toKotlin(node: Node<Any>) = jsPlanBody(this, node) { it }

/**
 * Turns a (possibly `async`) JS [body] into a plan body, running it with the [JsPlanScope] adapted by [scope].
 */
// ponytail: only promises returned by JsPlanScope resume within a step: code after awaiting any other promise
// (e.g., fetch) runs whenever the engine schedules it, even after the plan is stopped, unless wrapped in `external`.
@InternalJaktaAPI
fun <S> jsPlanBody(
    body: (S) -> Any?,
    node: Node<Any>,
    scope: (JsPlanScope) -> S,
): suspend context(Any)
(PlanScope<Any, Any, Any>) -> Any? = { planScope ->
    // Launching `achieve` in the plan's own coroutine context keeps the Intention it needs.
    val intention = CoroutineScope(currentCoroutineContext())
    @Suppress("TooGenericExceptionCaught")
    try {
        val result = body(scope(JsPlanScope(planScope.agent, node, intention, planScope.context)))
        if (result is Promise<*>) result.await() else result
    } catch (e: Throwable) {
        // A JS `Error` is not a Kotlin Exception: wrap it so the lifecycle handles it as a plan failure.
        throw e as? Exception ?: JsPlanFailure(e)
    }
}

/**
 * A plan failure caused by an error thrown by a JS plan body.
 */
class JsPlanFailure(cause: Throwable) : Exception("JS plan failed: ${cause.message}", cause)
