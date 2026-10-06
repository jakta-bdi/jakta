package it.unibo.jakta.llm

import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.PlanScope

/**
 * The context of a plan whose trigger is [meaning].
 * @property event the goal or belief that triggered the plan.
 * @property bindings the values that the trigger and the guards gave to the `{parameters}`.
 */
class LlmContext(val event: String, val bindings: Map<String, String>) {

    /**
     * Returns the value of [parameter] (with or without the curly braces).
     */
    operator fun get(parameter: String): String = parameter.removeSurrounding("{", "}").let {
        bindings[it] ?: error("Parameter {$it} is not bound in $bindings")
    }

    /**
     * Replaces the bound `{parameters}` of [text] with their values.
     */
    fun substitute(text: String): String = PARAMETER.replace(text) { bindings[it.groupValues[1]] ?: it.value }

    override fun toString(): String = "LlmContext(\"$event\", $bindings)"
}

// The closing brace is escaped for JS, whose unicode-mode regexes reject a lone '}'
private val PARAMETER = Regex("""\{(\w+)\}""")

/**
 * The `{parameters}` appearing in [text].
 */
internal fun parametersOf(text: String): Set<String> = PARAMETER.findAll(text).map { it.groupValues[1] }.toSet()

/**
 * The values of the `{parameters}` of [template] if [text] is [template] with some text in place of each parameter
 * (the same text for a repeated parameter), null otherwise.
 */
internal fun literalMatch(text: String, template: String): Map<String, String>? {
    val pattern = template.split(PARAMETER).joinToString("(.+?)") { Regex.escape(it) }
    val values = Regex(pattern).matchEntire(text)?.groupValues?.drop(1) ?: return null
    val pairs = PARAMETER.findAll(template).map { it.groupValues[1] }.zip(values.asSequence()).toList()
    return pairs.toMap().takeIf { bindings -> pairs.all { (name, value) -> bindings[name] == value } }
}

/**
 * A trigger that holds when this goal or belief matches the [template] (e.g. `"greet {name}"`),
 * literally or, asking the LLM, by meaning (e.g. `"say hi to Bob"`).
 * Plans are selected synchronously, so the agent blocks while the LLM answers.
 * @return the context binding the parameters of the [template], or null if there is no match
 * or the LLM gave no valid answer.
 */
context(reasoner: LlmReasoner)
fun String.meaning(template: String): LlmContext? =
    reasoner.blocking { match(this@meaning, template) }?.let { LlmContext(this, it) }

/**
 * A guard that holds when the [condition] (e.g. `"{name} is a friend"`), with the parameters bound so far replaced
 * by their values, is one of the beliefs or, asking the LLM, is implied by them.
 * Like [meaning], it blocks while the LLM answers.
 * @return the context, extended with the values of the parameters bound by the condition,
 * or null if it is false or the LLM gave no valid answer.
 */
context(reasoner: LlmReasoner)
fun GuardScope<String, LlmContext>.holds(condition: String): LlmContext? =
    reasoner.blocking { holds(context.substitute(condition), beliefs) }
        ?.let { LlmContext(context.event, context.bindings + it) }

/**
 * Asks whether [condition] is true according to the agent's beliefs, like a test goal.
 * In a plan triggered by [meaning], its bound parameters are replaced by their values first.
 * @return the values of the remaining `{parameters}` of the condition if it is true, null otherwise.
 */
context(reasoner: LlmReasoner)
suspend fun PlanScope<String, String, *>.test(condition: String): Map<String, String>? =
    reasoner.holds((context as? LlmContext)?.substitute(condition) ?: condition, agent.beliefs)

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
