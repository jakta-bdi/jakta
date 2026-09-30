package it.unibo.jakta.llm

import co.touchlab.kermit.Logger
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.plan.BasePlanScope
import it.unibo.jakta.plan.PlanScope

/**
 * The body of an [LlmPlan].
 */
typealias LlmPlanBody = suspend PlanScope<String, String, LlmContext>.() -> Unit

/**
 * The context of an [LlmPlan].
 * @property event the goal or belief that triggered the plan.
 * @property bindings the values the LLM gave to the `{parameters}` of the plan's trigger and condition.
 */
class LlmContext(val event: String, val bindings: Map<String, String>) {

    /**
     * Returns the value of [parameter] (with or without the curly braces).
     */
    operator fun get(parameter: String): String = parameter.removeSurrounding("{", "}").let {
        bindings[it] ?: error("Parameter {$it} is not bound in $bindings")
    }

    override fun toString(): String = "LlmContext(\"$event\", $bindings)"
}

/**
 * A plan whose [trigger] and optional [condition] are natural language, possibly with `{parameters}`,
 * selected by an [LlmReasoner].
 */
class LlmPlan internal constructor(val trigger: String, val condition: String?, internal val body: LlmPlanBody) {
    internal val parameters: Set<String> = parametersOf(trigger) + condition?.let(::parametersOf).orEmpty()
}

private val PARAMETER = Regex("""\{(\w+)\}""")

/**
 * The `{parameters}` appearing in [text].
 */
internal fun parametersOf(text: String): Set<String> = PARAMETER.findAll(text).map { it.groupValues[1] }.toSet()

/**
 * Collects the [LlmPlan]s of an agent, see [llmPlans].
 * Plans are considered in declaration order.
 */
class LlmPlanLibrary internal constructor() {
    internal val goals = mutableListOf<LlmPlan>()
    internal val beliefs = mutableListOf<LlmPlan>()
    internal val failures = mutableListOf<LlmPlan>()

    /**
     * A plan for the goals that are instances of [trigger], applicable when [onlyWhen] is true.
     */
    fun goal(trigger: String, onlyWhen: String? = null, body: LlmPlanBody) {
        goals += LlmPlan(trigger, onlyWhen, body)
    }

    /**
     * A plan for the added beliefs that are instances of [trigger], applicable when [onlyWhen] is true.
     */
    fun belief(trigger: String, onlyWhen: String? = null, body: LlmPlanBody) {
        beliefs += LlmPlan(trigger, onlyWhen, body)
    }

    /**
     * A plan for the failed goals that are instances of [trigger], applicable when [onlyWhen] is true.
     */
    fun failure(trigger: String, onlyWhen: String? = null, body: LlmPlanBody) {
        failures += LlmPlan(trigger, onlyWhen, body)
    }
}

/**
 * Adds the [LlmPlan]s declared in [block] to the plan library.
 *
 * Each kind of event (goal addition, belief addition, goal failure) handled by at least one LLM plan
 * gets a single JaKtA plan, relevant for *every* event of that kind: when it runs, one LLM call picks
 * the first LLM plan whose trigger matches the event and whose condition holds, and runs its body.
 * So JaKtA plans declared before this block take precedence, and those declared after it are shadowed.
 * When no LLM plan applies, the goal fails (or the failure stays unhandled); an added belief is just ignored.
 */
context(reasoner: LlmReasoner)
fun PlanLibraryBuilder<String, String>.llmPlans(block: LlmPlanLibrary.() -> Unit) {
    val library = LlmPlanLibrary().apply(block)
    if (library.goals.isNotEmpty()) {
        adding.goal { this } triggers { dispatch("goal added", library.goals, required = true) }
    }
    if (library.beliefs.isNotEmpty()) {
        adding.belief { this } triggers { dispatch("belief added", library.beliefs, required = false) }
    }
    if (library.failures.isNotEmpty()) {
        failing.goal { this } triggers { dispatch("goal failed", library.failures, required = true) }
    }
}

private val log = Logger.withTag("LlmPlans")

context(reasoner: LlmReasoner)
private suspend fun PlanScope<String, String, String>.dispatch(kind: String, plans: List<LlmPlan>, required: Boolean) {
    val event = "$kind: \"$context\""
    val selected = reasoner.select(event, agent.beliefs, plans)
    when {
        selected != null -> selected.first.body(BasePlanScope(agent, LlmContext(context, selected.second)))
        required -> error("No LLM plan is applicable for $event")
        else -> log.d { "No LLM plan is applicable for $event" }
    }
}

/**
 * Asks the LLM whether [condition] is true according to the agent's beliefs, like a test goal.
 * @return the values of the condition's `{parameters}` if it is true, null otherwise.
 */
context(reasoner: LlmReasoner)
suspend fun PlanScope<String, String, *>.test(condition: String): Map<String, String>? =
    reasoner.test(condition, agent.beliefs)

/**
 * Believes [belief], after forgetting the beliefs that the LLM says it contradicts or makes obsolete
 * (e.g., "the door is open" replaces "the door is closed").
 * Use `agent.believe` to add a belief without revision.
 */
context(reasoner: LlmReasoner)
suspend fun PlanScope<String, String, *>.revise(belief: String) {
    reasoner.contradicted(belief, agent.beliefs).forEach { agent.forget(it) }
    agent.believe(belief)
}
