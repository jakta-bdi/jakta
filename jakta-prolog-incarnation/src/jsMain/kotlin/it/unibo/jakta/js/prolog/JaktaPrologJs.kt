@file:OptIn(ExperimentalJsExport::class, InternalJaktaAPI::class)

package it.unibo.jakta.js.prolog

import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.js.JsAgent
import it.unibo.jakta.js.JsAgentDefinition
import it.unibo.jakta.js.JsPlanScope
import it.unibo.jakta.js.agent as jsAgent
import it.unibo.jakta.js.jsPlanBody
import it.unibo.jakta.logic.MutableSubstitutionPlanContext
import it.unibo.jakta.logic.annotatedMguWith
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.unifiesWith
import it.unibo.jakta.node.Node
import it.unibo.jakta.plan.BeliefAdditionPlan
import it.unibo.jakta.plan.BeliefRemovalPlan
import it.unibo.jakta.plan.GoalAdditionPlan
import it.unibo.jakta.plan.GoalFailurePlan
import it.unibo.jakta.plan.GoalRemovalPlan
import it.unibo.jakta.plan.GuardScope
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Numeric
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.parsing.TermParser
import it.unibo.tuprolog.solve.Solution
import kotlin.js.Promise
import kotlin.reflect.typeOf

/**
 * A plan body: a (possibly `async`) function receiving the [JsPrologPlanScope]. Its (awaited) return value
 * is the plan result, returned to whoever `achieve`d the goal.
 */
typealias JsPrologPlanBody = (JsPrologPlanScope) -> Any?

/**
 * Creates a new builder of an agent whose beliefs and goals are Prolog terms, optionally named [name].
 */
@JsExport
fun prologAgent(name: String? = null): JsPrologAgent = JsPrologAgent(jsAgent(name))

/**
 * JS-friendly fluent builder for a Prolog agent: beliefs, goals, triggers and guards are Prolog text
 * (e.g., `"start(N, X)"`), and the variables they bind are available to the plan body.
 * The package bundles `jakta-core`, whose `runMas` (and `setLogSeverity`) it exports: use them to run it.
 */
@JsExport
class JsPrologAgent internal constructor(private val agent: JsAgent) : JsAgentDefinition() {

    @JsExport.Ignore
    override fun toJsAgent(): JsAgent = agent

    /** Adds [beliefs] (facts, or rules such as `"big(X) :- limit(L), X >= L"`) to the initial beliefs. */
    fun believes(beliefs: Array<String>): JsPrologAgent = apply {
        agent.believes(beliefs.map(::belief).toTypedArray())
    }

    /** Adds [goals] to the initial goals. */
    fun hasInitialGoals(goals: Array<String>): JsPrologAgent = apply {
        agent.hasInitialGoals(goals.map { ground(parser.parseStruct(it)) }.toTypedArray())
    }

    /** Adds a plan triggered by the addition of a belief unifying with [trigger], if [guard] holds. */
    fun onBeliefAdded(trigger: String, body: JsPrologPlanBody, guard: String? = null): JsPrologAgent =
        plan(guard) { g ->
            addBeliefPlan(BeliefAdditionPlan(beliefTrigger(trigger), g, body.toKotlin(node), anyResult))
        }

    /** Adds a plan triggered by the removal of a belief unifying with [trigger], if [guard] holds. */
    fun onBeliefRemoved(trigger: String, body: JsPrologPlanBody, guard: String? = null): JsPrologAgent =
        plan(guard) { g -> addBeliefPlan(BeliefRemovalPlan(beliefTrigger(trigger), g, body.toKotlin(node), anyResult)) }

    /** Adds a plan triggered by the addition of a goal unifying with [trigger], if [guard] holds. */
    fun onGoalAdded(trigger: String, body: JsPrologPlanBody, guard: String? = null): JsPrologAgent =
        plan(guard) { g -> addGoalPlan(GoalAdditionPlan(goalTrigger(trigger), g, body.toKotlin(node), anyResult)) }

    /** Adds a plan triggered by the removal of a goal unifying with [trigger], if [guard] holds. */
    fun onGoalRemoved(trigger: String, body: JsPrologPlanBody, guard: String? = null): JsPrologAgent =
        plan(guard) { g -> addGoalPlan(GoalRemovalPlan(goalTrigger(trigger), g, body.toKotlin(node), anyResult)) }

    /** Adds a plan triggered by the failure of a goal unifying with [trigger], if [guard] holds. */
    fun onGoalFailed(trigger: String, body: JsPrologPlanBody, guard: String? = null): JsPrologAgent =
        plan(guard) { g -> addGoalPlan(GoalFailurePlan(goalTrigger(trigger), g, body.toKotlin(node), anyResult)) }

    private fun plan(
        guard: String?,
        add: it.unibo.jakta.dsl.agent.AgentBuilder<Any, Any, Any>.(GuardScope<Any, Any>.() -> Any?) -> Unit,
    ): JsPrologAgent = apply {
        val kotlinGuard = guard(guard)
        agent.configure { add(kotlinGuard) }
    }
}

/**
 * The scope available to the body of a Prolog plan. Terms passed as text can use the variables bound so far.
 */
@JsExport
class JsPrologPlanScope internal constructor(private val base: JsPlanScope) {
    private val planContext = base.context as MutableSubstitutionPlanContext

    /**
     * The variables bound so far (by the trigger, the guard and `query`): numbers, atoms as strings,
     * other terms as text.
     */
    val context: dynamic get() = planContext.substitution.toJs()

    /** A snapshot of the agent beliefs, as Prolog text. */
    val beliefs: Array<String> get() = beliefs().map { it.text() }.toTypedArray()

    /** The agent display name. */
    val agentName: String get() = base.agentName

    /** Adds a (ground) belief. */
    fun believe(belief: String) = base.believe(Fact.of(ground(parse(belief))))

    /** Removes a (ground) belief. */
    fun forget(belief: String) = base.forget(Fact.of(ground(parse(belief))))

    /** Logs a message on the agent output. */
    fun print(message: String) = base.print(message)

    /**
     * Solves [query] against the beliefs: on success, binds its variables for the rest of the plan
     * and returns the bindings (as [context] does), otherwise returns `null`.
     */
    fun query(query: String): dynamic = when (val solution = beliefs().unifiesWith(parse(query))) {
        is Solution.Yes -> {
            planContext += solution.substitution
            solution.substitution.toJs()
        }

        else -> null
    }

    /** Pursues the (ground) [goal] as a subgoal, resolving with the result of the plan that achieved it. */
    fun achieve(goal: String): Promise<Any?> = base.achieve(ground(parse(goal)))

    /** Pursues the (ground) [goal] in a new intention, without waiting for it. */
    fun alsoAchieve(goal: String) = base.alsoAchieve(ground(parse(goal)))

    /** Suspends the plan for [millis] milliseconds, like Kotlin's `delay`, letting other intentions run. */
    fun delay(millis: Int): Promise<Unit> = base.delay(millis)

    /** Waits for a promise not created by JaKtA (e.g., `fetch`) as a suspension of the plan. */
    fun external(promise: Promise<Any?>): Promise<Any?> = base.external(promise)

    /** Terminates the node the agent is running on. */
    fun terminateNode() = base.terminateNode()

    private fun beliefs(): List<PrologBelief> = base.beliefs.map { it as PrologBelief }

    private fun parse(text: String): Struct = parser.parseStruct(text).bound(planContext)
}

private val parser = TermParser.withDefaultOperators()

// JS plans are untyped: declaring Unit keeps them relevant both for `achieve` (Unit) and JS `achieve` (Any?).
private val anyResult = typeOf<Unit>()

private fun JsPrologPlanBody.toKotlin(node: Node<Any>) = jsPlanBody(this, node, ::JsPrologPlanScope)

private fun goalTrigger(text: String): (Any) -> Any? {
    val query = parser.parseStruct(text)
    return { goal ->
        (goal as Struct).annotatedMguWith(query).takeUnless { it is Substitution.Fail }
            ?.let(::MutableSubstitutionPlanContext)
    }
}

private fun beliefTrigger(text: String): (Any) -> Any? {
    val query = parser.parseStruct(text)
    return { belief -> (belief as PrologBelief).matchBelief(query) }
}

@Suppress("UNCHECKED_CAST")
private fun guard(text: String?): GuardScope<Any, Any>.() -> Any? {
    val query = text?.let(parser::parseStruct) ?: return { context }
    return {
        val planContext = context as MutableSubstitutionPlanContext
        when (val solution = (beliefs as Collection<PrologBelief>).unifiesWith(query.bound(planContext))) {
            is Solution.Yes -> MutableSubstitutionPlanContext(planContext.substitution + solution.substitution)
            else -> null
        }
    }
}

/**
 * Replaces the variables bound in [context], matched by name: each piece of text is parsed on its own,
 * so the same variable name in the trigger, the guard and the body is a different [it.unibo.tuprolog.core.Var].
 */
private fun Struct.bound(context: MutableSubstitutionPlanContext): Struct {
    val byName = context.substitution.entries.associate { (variable, value) -> variable.name to value }
    val bindings = variables.distinct().filterNot { it.isAnonymous }
        .mapNotNull { variable -> byName[variable.name]?.let { variable to it } }
    return apply(Substitution.of(bindings.toList())).castToStruct()
}

private fun belief(text: String): Rule = when (val struct = parser.parseStruct(text)) {
    is Rule -> struct
    else -> Fact.of(ground(struct))
}

private fun ground(struct: Struct): Struct = struct.also { requireGround(it) { "Term must be ground, but got $it" } }

private fun Substitution.toJs(): dynamic {
    val bindings: dynamic = js("({})")
    forEach { (variable, value) -> if (!variable.isAnonymous) bindings[variable.name] = value.toJs() }
    return bindings
}

private fun Term.toJs(): Any = when (this) {
    is Numeric -> decimalValue.toDouble()
    is Atom -> value
    else -> toString()
}

private fun Rule.text(): String = if (this is Fact) head.toString() else toString()
