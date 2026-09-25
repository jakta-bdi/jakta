@file:OptIn(ExperimentalJsExport::class, InternalJaktaAPI::class, DelicateCoroutinesApi::class)

package it.unibo.jakta.js

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.BaseMutableAgentState
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.BeliefAdditionPlan
import it.unibo.jakta.plan.BeliefRemovalPlan
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.GoalFailurePlan
import it.unibo.jakta.plan.GoalRemovalPlan
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.PlanScope
import kotlin.js.Promise
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.promise

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
fun runMas(agents: Array<JsAgent>): Promise<Unit> = GlobalScope.promise {
    mas(NodeBuilders.baseNode<Any>()) {
        node { withAgents(*agents.map { it.toSpecification() }.toTypedArray()) }
    }.runLocally()
}

/**
 * JS-friendly fluent builder for an agent whose beliefs and goals are arbitrary JS values
 * (compared with Kotlin equality, i.e. `===` for plain JS objects).
 */
@JsExport
class JsAgent internal constructor(private val name: String?) {
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

    /** Pursues [goal] in a new intention, without waiting for it. */
    fun alsoAchieve(goal: Any) = agent.alsoAchieve(goal)

    /** Terminates the node the agent is running on. */
    fun terminateNode() = node.terminateNode()

    /**
     * Runs [block] in the plan's intention, started undispatched so it begins within the current step
     * (as Kotlin's suspending calls do). The returned promise settles within a later step of the intention, and the
     * JS code it resumes is then run by the engine's microtask queue: the agent waits for it before its next event,
     * so that code still belongs to that step, as the code after a Kotlin suspension point does.
     */
    private fun <T> inIntention(block: suspend () -> T): Promise<T> =
        intention.promise(start = CoroutineStart.UNDISPATCHED) {
            try {
                block()
            } finally {
                (agent as? BaseMutableAgentState<*, *>)?.beforeNextEvent?.add(::drainMicrotasks)
            }
        }
}

// A macrotask only runs once the microtask queue is empty, i.e., once the resumed JS code reached its next `await`.
// ponytail: browsers clamp nested setTimeout to 4ms, switch to MessageChannel if JS plans in browsers are too slow.
private suspend fun drainMicrotasks() = Promise<Unit> { resolve, _ ->
    js("(typeof setImmediate === 'function' ? setImmediate : setTimeout)")(resolve)
}.await()

// JS plans are untyped: declaring Unit keeps them relevant both for `achieve` (Unit) and JS `achieve` (Any?).
private val anyResult = typeOf<Unit>()

private fun JsGuard?.toKotlin(): GuardScope<Any, Any>.() -> Any? =
    this?.let { guard -> { guard(JsGuardScope(beliefs.toTypedArray(), context)) } } ?: { context }

// ponytail: only promises returned by JsPlanScope resume within a step: code after awaiting any other promise
// (e.g., fetch) runs whenever the engine schedules it, and is not stopped when the intention is cancelled.
private fun JsPlanBody.toKotlin(node: Node<Any>): suspend context(Any)
(PlanScope<Any, Any, Any>) -> Any? = { scope ->
    // Launching `achieve` in the plan's own coroutine context keeps the Intention it needs.
    val intention = CoroutineScope(currentCoroutineContext())
    @Suppress("TooGenericExceptionCaught")
    try {
        val result = this(JsPlanScope(scope.agent, node, intention, scope.context))
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
