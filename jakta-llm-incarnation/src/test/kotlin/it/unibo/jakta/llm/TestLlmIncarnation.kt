package it.unibo.jakta.llm

import ai.koog.agents.testing.tools.getMockExecutor
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.model.PromptExecutor
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.PlanLibraryBuilder
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.plan.PlanScope
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestLlmIncarnation {

    private val log = mutableListOf<String>()

    /**
     * The user messages sent to the LLM.
     */
    private val prompts = mutableListOf<String>()

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Assert)
    }

    /**
     * A scripted answer of the LLM.
     */
    private fun answer(holds: Boolean, vararg bindings: Pair<String, String>): String = """
        {"reason": "scripted", "holds": $holds,
         "bindings": [${bindings.joinToString { (p, v) -> """{"parameter": "$p", "value": "$v"}""" }}]}
    """.trimIndent()

    /**
     * An executor recording the [prompts] and giving the first of the [answers] whose key the prompt contains.
     */
    private fun llm(vararg answers: Pair<String, String>): PromptExecutor = getMockExecutor {
        mockLLMAnswer("unused") onCondition {
            prompts += it
            false
        }
        answers.forEach { (key, answer) -> mockLLMAnswer(answer) onCondition { key in it } }
        mockLLMAnswer("I cannot tell").asDefaultResponse
    }

    private fun runMas(
        executor: PromptExecutor,
        beliefs: List<String> = emptyList(),
        goal: String,
        expected: List<String>,
        maxQuestions: Int = Int.MAX_VALUE,
        plans: context(NodeTerminationSkill, LlmReasoner) PlanLibraryBuilder<String, String>.() -> Unit,
    ): TestResult = runTest {
        launch {
            mas(NodeBuilders.baseNode()) {
                node {
                    // A model with native structured output, so Koog sends the prompt unchanged to the mock.
                    val reasoner = LlmReasoner(executor, OpenAIModels.Chat.GPT5_6Luna, maxQuestions)
                    context(NodeTerminationSkill(node), reasoner) {
                        agent {
                            embodiedAs { Any() }
                            believes { beliefs.forEach { +it } }
                            hasInitialGoals { !goal }
                            hasPlanLibrary { plans() }
                        }
                    }
                }
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }.join()
        assertEquals(expected, log)
    }

    /**
     * Records the failed goal, and ends the test.
     */
    context(_: NodeTerminationSkill)
    private fun PlanLibraryBuilder<String, String>.failureRecorder() {
        failing.goal { this } triggers {
            log += "failed: $context"
            terminateNode()
        }
    }

    context(_: NodeTerminationSkill)
    private fun PlanScope<*, *, *>.wrongPlan() {
        log += "wrong plan"
        terminateNode()
    }

    @Test
    fun `plans are tried in order, with triggers and guards matched by meaning`(): TestResult {
        val executor = llm(
            "Trigger: \"greet {name}\"" to answer(true, "name" to "Bob"),
            "Statement: \"Bob is an enemy\"" to answer(false),
        )
        return runMas(executor, listOf("Bob is a friend"), "say hi to Bob", listOf("Hello, Bob!")) {
            adding.goal { meaning("greet {name}") } onlyWhen { holds("{name} is an enemy") } triggers { wrongPlan() }
            adding.goal { meaning("greet {name}") } onlyWhen { holds("{name} is a friend") } triggers {
                // The trigger is asked once, though the engine evaluates both plans' triggers more than once,
                // and the second guard holds literally, without asking the LLM
                assertEquals(
                    listOf(
                        "Event: \"say hi to Bob\"\nTrigger: \"greet {name}\"\nParameters: name",
                        "Beliefs:\n1. Bob is a friend\nStatement: \"Bob is an enemy\"\nParameters: none",
                    ),
                    prompts,
                )
                log += "Hello, ${context["name"]}!"
                terminateNode()
            }
        }
    }

    @Test
    fun `a literal match binds the parameters without asking the LLM`(): TestResult =
        runMas(llm(), goal = "set the heating to 21 degrees", expected = listOf("21 degrees, 0 prompts")) {
            adding.goal { meaning("set the heating to {degrees} degrees") } triggers {
                log += "${context["degrees"]} degrees, ${prompts.size} prompts"
                terminateNode()
            }
        }

    @Test
    fun `a goal that matches no plan fails, and a failure plan handles it`(): TestResult {
        val executor = llm("Trigger: \"greet {name}\"" to answer(false))
        return runMas(executor, goal = "order a pizza", expected = listOf("I cannot order a pizza")) {
            adding.goal { meaning("greet {name}") } triggers { wrongPlan() }
            failing.goal { meaning("{task}") } triggers {
                log += "I cannot ${context["task"]}"
                terminateNode()
            }
        }
    }

    @Test
    fun `a malformed answer fails the goal, not the agent`(): TestResult =
        runMas(llm(), goal = "say hi to Bob", expected = listOf("failed: say hi to Bob")) {
            failureRecorder()
            adding.goal { meaning("greet {name}") } triggers { wrongPlan() }
        }

    @Test
    fun `a match that leaves a parameter unbound fails the goal`(): TestResult {
        val executor = llm("Trigger: \"greet {name}\"" to answer(true))
        return runMas(executor, goal = "say hi to Bob", expected = listOf("failed: say hi to Bob")) {
            failureRecorder()
            adding.goal { meaning("greet {name}") } triggers { wrongPlan() }
        }
    }

    @Test
    fun `added beliefs trigger plans by meaning`(): TestResult {
        val executor = llm("Trigger: \"the temperature is {degrees} degrees\"" to answer(true, "degrees" to "30"))
        val expected = listOf("it is 30 degrees outside -> 30")
        return runMas(executor, goal = "check the thermometer", expected = expected) {
            adding.goal { takeIf { it == "check the thermometer" } } triggers {
                agent.believe("it is 30 degrees outside")
            }
            adding.belief { meaning("the temperature is {degrees} degrees") } triggers {
                log += "${context.event} -> ${context["degrees"]}"
                terminateNode()
            }
        }
    }

    @Test
    fun `tests query the beliefs and revisions replace contradicted beliefs`(): TestResult {
        val executor = llm(
            "Statement: \"someone is home\"" to answer(true),
            "Statement: \"the window is open\"" to answer(false),
            "New belief" to """{"reason": "scripted", "contradicted": [1]}""",
        )
        val beliefs = listOf("the door is closed", "the light is on")
        val expected = listOf("{state=closed}", "{}", "null", "[the light is on, the door is open]")
        return runMas(executor, beliefs, goal = "go", expected = expected) {
            adding.goal { takeIf { it == "go" } } triggers {
                log += test("the door is {state}").toString() // literally a belief
                log += test("someone is home").toString()
                log += test("the window is open").toString()
                revise("the door is open")
                log += agent.beliefs.toList().toString()
                terminateNode()
            }
        }
    }

    @Test
    fun `the question budget stops a plan that the LLM keeps choosing for its own subgoal`(): TestResult {
        val executor = llm("Trigger: \"keep going\"" to answer(true))
        return runMas(executor, goal = "go on", expected = listOf("failed: go on"), maxQuestions = 3) {
            failureRecorder()
            adding.goal { meaning("keep going") } triggers { agent.achieve(context.event) }
        }
    }
}
