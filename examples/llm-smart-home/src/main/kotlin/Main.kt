import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLModel
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.llm.LlmReasoner
import it.unibo.jakta.llm.llmPlans
import it.unibo.jakta.llm.revise
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlinx.coroutines.runBlocking

/**
 * The requests the smart home receives, in natural language.
 */
val requests = listOf(
    "Brr, it's freezing in here!",
    "Could you order me a pizza?",
    "Make it 23 degrees, please.",
)

/**
 * More than enough for the [requests]: a cap on the cost of a model that keeps choosing the wrong plan.
 */
const val MAX_CALLS = 20

/**
 * A smart-home agent that handles each of the [requests] with the plan chosen by the LLM of the [reasoner],
 * then passes its final beliefs to [onDone].
 */
fun smartHome(reasoner: LlmReasoner, requests: List<String>, onDone: (List<String>) -> Unit = {}) = with(reasoner) {
    agent<String, String, Any> {
        embodiedAs { Any() }
        believes {
            +"the living room window is open"
            +"the heating is off"
        }
        hasInitialGoals { !"serve" }
        hasPlanLibrary {
            adding.goal { takeIf { it == "serve" } } triggers {
                requests.forEach {
                    agent.print("> $it")
                    agent.achieve(it)
                }
                onDone(agent.beliefs.toList())
                node.terminateNode()
            }
            llmPlans {
                goal("make the room warmer", onlyWhen = "a window is open") {
                    agent.print("Closing the window first.")
                    agent.forget("the living room window is open")
                    agent.believe("the living room window is closed")
                    agent.achieve("set the heating to 21 degrees")
                }
                goal("make the room warmer") {
                    agent.achieve("set the heating to 21 degrees")
                }
                goal("set the heating to {degrees} degrees") {
                    agent.print("Setting the heating to ${context["degrees"]} degrees.")
                    revise("the heating is set to ${context["degrees"]} degrees")
                }
            }
            // A plain JaKtA plan, so failures are handled without calling the LLM
            failing.goal { this } triggers {
                agent.print("Sorry, I cannot handle \"$context\".")
            }
        }
    }
}

/**
 * The executor and model of the provider named by `JAKTA_LLM_PROVIDER` (`anthropic`, `openai` or `ollama`).
 * By default, the first provider with an API key (`ANTHROPIC_API_KEY`, `OPENAI_API_KEY`), else a local Ollama.
 * `JAKTA_LLM_MODEL` replaces the id of the provider's default model (keeping its declared capabilities).
 */
fun llmFromEnvironment(): Pair<PromptExecutor, LLModel> {
    fun key(name: String) = checkNotNull(System.getenv(name)) { "$name is not set" }
    val provider = System.getenv("JAKTA_LLM_PROVIDER") ?: when {
        System.getenv("ANTHROPIC_API_KEY") != null -> "anthropic"
        System.getenv("OPENAI_API_KEY") != null -> "openai"
        else -> "ollama"
    }
    val (executor, model) = when (provider) {
        "anthropic" -> MultiLLMPromptExecutor(AnthropicLLMClient(key("ANTHROPIC_API_KEY"))) to AnthropicModels.Haiku_4_5
        "openai" -> MultiLLMPromptExecutor(OpenAILLMClient(key("OPENAI_API_KEY"))) to OpenAIModels.Chat.GPT5_6Luna
        "ollama" -> MultiLLMPromptExecutor(OllamaClient()) to OllamaModels.Meta.LLAMA_3_2
        else -> error("Unknown JAKTA_LLM_PROVIDER '$provider', use anthropic, openai or ollama")
    }
    return executor to (System.getenv("JAKTA_LLM_MODEL")?.let { model.copy(id = it) } ?: model)
}

/**
 * Runs the smart home with the LLM configured by the environment, see [llmFromEnvironment].
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert) // Only agent.print; use Debug to also see why the LLM chose each plan
    val (executor, model) = llmFromEnvironment()
    executor.use {
        mas(NodeBuilders.baseNode()) {
            node {
                withAgents(smartHome(LlmReasoner(it, model, MAX_CALLS), requests) { println("Beliefs: $it") })
            }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
    }
}
