package it.unibo.jakta.llm

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import co.touchlab.kermit.Logger
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Does, with a language [model] reached through a Koog [executor], the reasoning that other incarnations do
 * with matching and unification, on beliefs and goals written in natural language.
 * Every method is a single structured-output call; malformed answers and executor errors are thrown.
 * Since the LLM may send a goal back to the plan that posted it, looping forever, calls beyond [maxCalls] throw too.
 *
 * Put it in scope with `with(LlmReasoner(executor, model)) { agent { ... } }` to use
 * [llmPlans], [test] and [revise].
 */
@OptIn(ExperimentalAtomicApi::class)
class LlmReasoner(
    private val executor: PromptExecutor,
    private val model: LLModel,
    private val maxCalls: Int = Int.MAX_VALUE,
) {

    private val log = Logger.withTag("LlmReasoner")

    private val calls = AtomicInt(0)

    // As deterministic as the model allows (e.g., GPT-5 models accept no temperature)
    private val params = LLMParams(temperature = 0.0.takeIf { model.supports(LLMCapability.Temperature) })

    /**
     * Returns the first of the [plans] that is relevant for the [event] (e.g. `goal added: "greet bob"`)
     * and applicable given the [beliefs], with the values of its parameters, or null if there is none.
     */
    internal suspend fun select(
        event: String,
        beliefs: Collection<String>,
        plans: List<LlmPlan>,
    ): Pair<LlmPlan, Map<String, String>>? {
        val options = plans.map { plan ->
            "trigger: \"${plan.trigger}\"" + plan.condition?.let { "; condition: \"$it\"" }.orEmpty()
        }
        return choose(SELECT_PLAN, beliefs, "Event: $event", options) { plans[it].parameters }
            ?.let { (index, bindings) -> plans[index] to bindings }
    }

    /**
     * Returns the values of the parameters of [condition] if it is true given the [beliefs], null otherwise.
     */
    internal suspend fun test(condition: String, beliefs: Collection<String>): Map<String, String>? =
        choose(TEST, beliefs, "Is this statement true?", listOf("\"$condition\"")) { parametersOf(condition) }
            ?.second

    /**
     * Returns the [beliefs] that the new [belief] contradicts or makes obsolete.
     */
    internal suspend fun contradicted(belief: String, beliefs: Collection<String>): List<String> {
        if (beliefs.isEmpty()) return emptyList()
        val numbered = beliefs.toList()
        val request = prompt("jakta-llm-revise", params) {
            system(REVISE)
            user("${numbered("Beliefs", numbered)}\nNew belief: \"$belief\"")
        }
        val revision = ask<Revision>(request)
        log.d { "New belief \"$belief\" contradicts ${revision.contradicted} (${revision.reason})" }
        require(revision.contradicted.all { it in 1..numbered.size }) {
            "The LLM retracted beliefs ${revision.contradicted}, but only 1 to ${numbered.size} exist"
        }
        return revision.contradicted.map { numbered[it - 1] }
    }

    /**
     * Asks the LLM for the first of the [options] that holds, returning its index and parameter bindings,
     * or null if none holds.
     * Fails if the answer is malformed, or does not bind all the [parameters] of the chosen option.
     */
    private suspend fun choose(
        instructions: String,
        beliefs: Collection<String>,
        question: String,
        options: List<String>,
        parameters: (Int) -> Set<String>,
    ): Pair<Int, Map<String, String>>? {
        val described = options.mapIndexed { i, option ->
            option + parameters(i).takeIf { it.isNotEmpty() }?.joinToString(prefix = "; parameters: ").orEmpty()
        }
        val request = prompt("jakta-llm-choose", params) {
            system(instructions)
            user("${numbered("Beliefs", beliefs)}\n$question\n${numbered("Options", described)}")
        }
        val choice = ask<Choice>(request)
        log.d { "$question -> option ${choice.option} ${choice.bindings} (${choice.reason})" }
        if (choice.option == 0) return null
        require(choice.option in 1..options.size) {
            "The LLM chose option ${choice.option}, but only 1 to ${options.size} exist"
        }
        val index = choice.option - 1
        val bindings = choice.bindings.associate { it.parameter.removeSurrounding("{", "}") to it.value }
        val unbound = parameters(index) - bindings.keys
        require(unbound.isEmpty()) { "The LLM chose option ${choice.option} without binding $unbound" }
        return index to bindings
    }

    private suspend inline fun <reified T> ask(request: Prompt): T {
        check(calls.incrementAndFetch() <= maxCalls) { "The budget of $maxCalls LLM calls is exhausted" }
        return executor.executeStructured<T>(request, model).getOrThrow().data
    }

    private companion object {
        const val BASE = "You are the reasoning engine of a BDI agent. " +
            "Its beliefs, events, plans and conditions are written in natural language. " +
            "A statement is true when the beliefs state or clearly imply it: do not assume anything else. " +
            "Words in curly braces, like {name}, are parameters: for the option you choose, give one binding " +
            "to each of its parameters, whose value is what the parameter stands for in the event or in the beliefs. "

        const val SELECT_PLAN = BASE +
            "A plan is relevant when the event matches its trigger: the event is, means, or asks for what the " +
            "trigger describes, possibly in other words, with its parameters taking specific values. " +
            "A relevant plan is applicable when its condition, if any, is true. " +
            "Choose the first applicable plan, answering with its number, or with 0 if no plan is applicable."

        const val TEST = BASE + "Answer 1 if the statement of option 1 is true, 0 otherwise."

        const val REVISE = "You maintain the beliefs of a BDI agent, written in natural language. " +
            "A new belief is being added: list the numbers of the current beliefs that it contradicts " +
            "or makes obsolete, and must therefore be forgotten. " +
            "Beliefs that are merely related to the new one must be kept."

        fun numbered(title: String, items: Collection<String>): String = if (items.isEmpty()) {
            "$title: none"
        } else {
            items.withIndex().joinToString("\n", prefix = "$title:\n") { (i, item) -> "${i + 1}. $item" }
        }
    }
}

/**
 * The structured answer of the LLM when choosing among options.
 */
@Serializable
@SerialName("Choice")
internal data class Choice(
    @property:LLMDescription("A short explanation of the choice.")
    val reason: String,
    @property:LLMDescription("The number of the chosen option, or 0 if no option holds.")
    val option: Int,
    @property:LLMDescription("The value of every {parameter} of the chosen option.")
    val bindings: List<Binding>,
)

/**
 * The value of a parameter.
 */
@Serializable
@SerialName("Binding")
internal data class Binding(val parameter: String, val value: String)

/**
 * The structured answer of the LLM when revising beliefs.
 */
@Serializable
@SerialName("Revision")
internal data class Revision(
    @property:LLMDescription("A short explanation of the revision.")
    val reason: String,
    @property:LLMDescription("The numbers of the beliefs to forget.")
    val contradicted: List<Int>,
)
