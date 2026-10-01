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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Does, with a language [model] reached through a Koog [executor], the matching that other incarnations do
 * with equality or unification, on beliefs and goals written in natural language with `{parameters}`.
 * Texts that match literally (e.g. `greet bob` and `greet {name}`) need no LLM call; otherwise every question is
 * a single structured-output call, whose answer is remembered, since the engine evaluates the trigger and the guard
 * of a plan more than once while selecting and running it.
 * Malformed answers and executor errors are thrown, except by triggers and guards, which do not hold instead.
 * Since an LLM may send a goal back to the plan that posted it, looping forever, questions beyond [maxQuestions]
 * (including those answered from memory) throw too.
 *
 * Put it in scope with `with(LlmReasoner(executor, model)) { agent { ... } }` to use
 * [meaning], [holds], [test] and [revise].
 */
@OptIn(ExperimentalAtomicApi::class)
class LlmReasoner(
    private val executor: PromptExecutor,
    private val model: LLModel,
    private val maxQuestions: Int = Int.MAX_VALUE,
) {

    private val log = Logger.withTag("LlmReasoner")

    private val questions = AtomicInt(0)

    // ponytail: only the latest answers, enough for the repeated evaluations of a plan selection
    private val answers = object : LinkedHashMap<String, Map<String, String>?>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Map<String, String>?>) =
            size > REMEMBERED_ANSWERS
    }

    // As deterministic as the model allows (e.g., GPT-5 models accept no temperature)
    private val params = LLMParams(temperature = 0.0.takeIf { model.supports(LLMCapability.Temperature) })

    /**
     * Runs [question] for a trigger or a guard, which cannot suspend, blocking the calling thread.
     * @return its answer, or null if it failed: throwing would stop the agent instead of the plan selection.
     */
    @Suppress("TooGenericExceptionCaught")
    internal fun <T : Any> blocking(question: suspend LlmReasoner.() -> T?): T? = try {
        runBlocking { question() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        log.e(e) { "No valid answer, so the trigger or guard does not hold" }
        null
    }

    /**
     * Returns the values of the parameters of the [trigger] if the [event] matches it, null otherwise.
     */
    internal suspend fun match(event: String, trigger: String): Map<String, String>? =
        literalMatch(event, trigger) ?: ask(MATCH, "Event: \"$event\"\nTrigger: \"$trigger\"", trigger)

    /**
     * Returns the values of the parameters of the [statement] if it is true given the [beliefs], null otherwise.
     */
    internal suspend fun holds(statement: String, beliefs: Collection<String>): Map<String, String>? {
        val numbered = beliefs.toList()
        return numbered.firstNotNullOfOrNull { literalMatch(it, statement) }
            ?: ask(HOLDS, "${numbered("Beliefs", numbered)}\nStatement: \"$statement\"", statement)
    }

    /**
     * Returns the [beliefs] that the new [belief] contradicts or makes obsolete.
     */
    internal suspend fun contradicted(belief: String, beliefs: Collection<String>): List<String> {
        if (beliefs.isEmpty()) return emptyList()
        spendQuestion()
        val numbered = beliefs.toList()
        val request = prompt("jakta-llm-revise", params) {
            system(REVISE)
            user("${numbered("Beliefs", numbered)}\nNew belief: \"$belief\"")
        }
        val revision = structured<Revision>(request)
        log.d { "New belief \"$belief\" contradicts ${revision.contradicted} (${revision.reason})" }
        require(revision.contradicted.all { it in 1..numbered.size }) {
            "The LLM retracted beliefs ${revision.contradicted}, but only 1 to ${numbered.size} exist"
        }
        return revision.contradicted.map { numbered[it - 1] }
    }

    /**
     * Asks whether the [question] holds, returning the values of the parameters of the [template] if it does.
     * Fails if the LLM answers that it holds without binding all the parameters.
     */
    private suspend fun ask(instructions: String, question: String, template: String): Map<String, String>? {
        val parameters = parametersOf(template)
        val message = "$question\nParameters: ${parameters.joinToString().ifEmpty { "none" }}"
        spendQuestion()
        val key = instructions + message
        synchronized(answers) { if (key in answers) return answers[key] }
        val request = prompt("jakta-llm-ask", params) {
            system(instructions)
            user(message)
        }
        val answer = structured<Answer>(request)
        log.d { "$question -> ${answer.holds} ${answer.bindings} (${answer.reason})" }
        val bindings = answer.bindings
            .associate { it.parameter.removeSurrounding("{", "}") to it.value }
            .filterKeys { it in parameters }
            .takeIf { answer.holds }
        require(bindings == null || bindings.keys == parameters) {
            "The LLM answered yes without binding ${parameters - bindings.orEmpty().keys} to: $question"
        }
        return bindings.also { synchronized(answers) { answers[key] = it } }
    }

    private fun spendQuestion() =
        check(questions.incrementAndFetch() <= maxQuestions) { "The budget of $maxQuestions questions is exhausted" }

    private suspend inline fun <reified T> structured(request: Prompt): T =
        executor.executeStructured<T>(request, model).getOrThrow().data

    private companion object {
        const val REMEMBERED_ANSWERS = 100

        const val BASE = "You are the reasoning engine of a BDI agent, whose goals, beliefs and plans are written " +
            "in natural language. Words in curly braces, like {name}, are parameters: if your answer is yes, " +
            "give one binding to each parameter, whose value is what the parameter stands for. "

        const val MATCH = BASE +
            "Answer whether the event matches the trigger of a plan: whether it is, means, or asks for what the " +
            "trigger describes, possibly in other words, with its parameters taking specific values."

        const val HOLDS = BASE +
            "Answer whether the statement is true according to the beliefs only: they state it or clearly imply " +
            "it. Do not assume anything else."

        const val REVISE = "You maintain the beliefs of a BDI agent, written in natural language. " +
            "A new belief is being added: list the numbers of the current beliefs that it contradicts " +
            "or makes obsolete, and must therefore be forgotten. " +
            "Beliefs that are merely related to the new one must be kept."

        fun numbered(title: String, items: List<String>): String = if (items.isEmpty()) {
            "$title: none"
        } else {
            items.withIndex().joinToString("\n", prefix = "$title:\n") { (i, item) -> "${i + 1}. $item" }
        }
    }
}

/**
 * The structured answer of the LLM to a yes-or-no question.
 */
@Serializable
@SerialName("Answer")
internal data class Answer(
    @property:LLMDescription("A short explanation of the answer.")
    val reason: String,
    @property:LLMDescription("Whether the event matches the trigger, or the statement is true.")
    val holds: Boolean,
    @property:LLMDescription("If the answer is yes, the value of every parameter, otherwise nothing.")
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
