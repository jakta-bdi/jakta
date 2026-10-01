import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.llm.LlmReasoner
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * Runs the example against a real LLM: skipped unless one is configured,
 * with `JAKTA_LLM_PROVIDER` (e.g. `ollama`), `ANTHROPIC_API_KEY` or `OPENAI_API_KEY`.
 */
class SmartHomeTest {
    @Test
    fun `the smart home closes the window and revises the heating`() {
        assumeTrue(
            listOf("JAKTA_LLM_PROVIDER", "ANTHROPIC_API_KEY", "OPENAI_API_KEY").any { System.getenv(it) != null },
            "No LLM configured",
        )
        Logger.setMinSeverity(Severity.Debug)
        var beliefs = emptyList<String>()
        val (executor, model) = llmFromEnvironment()
        executor.use {
            runBlocking {
                withTimeout(10.minutes) {
                    mas(NodeBuilders.baseNode()) {
                        node { withAgents(smartHome(LlmReasoner(it, model, MAX_QUESTIONS), requests) { beliefs = it }) }
                    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
                }
            }
        }
        assertContains(beliefs, "the living room window is closed")
        assertContains(beliefs, "the heating is set to 23 degrees")
        assertFalse("the heating is off" in beliefs || "the heating is set to 21 degrees" in beliefs, "$beliefs")
    }
}
