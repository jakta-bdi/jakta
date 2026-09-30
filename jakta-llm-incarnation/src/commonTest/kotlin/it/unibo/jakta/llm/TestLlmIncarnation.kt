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
import it.unibo.jakta.skills.NodeTerminationSkill
import it.unibo.jakta.skills.terminateNode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestLlmIncarnation {

    private val log = mutableListOf<String>()

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Assert)
    }

    /**
     * A scripted answer of the LLM choosing an option.
     */
    private fun choice(option: Int, vararg bindings: Pair<String, String>): String = """
        {"reason": "scripted", "option": $option,
         "bindings": [${bindings.joinToString { (p, v) -> """{"parameter": "$p", "value": "$v"}""" }}]}
    """.trimIndent()

    private fun runMas(
        executor: PromptExecutor,
        beliefs: List<String> = emptyList(),
        goal: String,
        expected: List<String>,
        maxCalls: Int = Int.MAX_VALUE,
        plans: context(NodeTerminationSkill, LlmReasoner) PlanLibraryBuilder<String, String>.() -> Unit,
    ): TestResult = runTest {
        launch {
            mas(NodeBuilders.baseNode()) {
                node {
                    // A model with native structured output, so Koog sends the prompt unchanged to the mock.
                    context(NodeTerminationSkill(node), LlmReasoner(executor, OpenAIModels.Chat.GPT5_6Luna, maxCalls)) {
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

    @Test
    fun `the LLM selects the first applicable plan and binds its parameters`(): TestResult {
        val prompts = mutableListOf<String>()
        val executor = getMockExecutor {
            mockLLMAnswer(choice(2, "name" to "Bob")) onCondition {
                prompts += it
                "say hi to Bob" in it
            }
        }
        val expectedPrompt = """
            Beliefs:
            1. Bob is a friend
            Event: goal added: "say hi to Bob"
            Options:
            1. trigger: "greet {name}"; condition: "{name} is an enemy"; parameters: name
            2. trigger: "greet {name}"; condition: "{name} is a friend"; parameters: name
        """.trimIndent()
        return runMas(executor, listOf("Bob is a friend"), "say hi to Bob", expected = listOf("Hello, Bob!")) {
            llmPlans {
                goal("greet {name}", onlyWhen = "{name} is an enemy") { fail("wrong plan") }
                goal("greet {name}", onlyWhen = "{name} is a friend") {
                    assertEquals(listOf(expectedPrompt), prompts)
                    log += "Hello, ${context["name"]}!"
                    terminateNode()
                }
            }
        }
    }

    @Test
    fun `a goal with no applicable plan fails and is handled by a failure plan`(): TestResult {
        val executor = getMockExecutor {
            mockLLMAnswer(choice(0)) onRequestContains "goal added"
            mockLLMAnswer(choice(1, "task" to "order a pizza")) onRequestContains "goal failed"
        }
        return runMas(executor, goal = "order a pizza", expected = listOf("I cannot order a pizza")) {
            llmPlans {
                goal("greet {name}") { fail("wrong plan") }
                failure("{task}") {
                    log += "I cannot ${context["task"]}"
                    terminateNode()
                }
            }
        }
    }

    @Test
    fun `a malformed answer makes the goal fail`(): TestResult {
        val executor = getMockExecutor { mockLLMAnswer("I would pick the first plan").asDefaultResponse }
        return runMas(executor, goal = "say hi to Bob", expected = listOf("failed: say hi to Bob")) {
            failing.goal { this } triggers {
                log += "failed: $context"
                terminateNode()
            }
            llmPlans { goal("greet {name}") { fail("no plan should run") } }
        }
    }

    @Test
    fun `choosing a plan without binding its parameters makes the goal fail`(): TestResult {
        val executor = getMockExecutor { mockLLMAnswer(choice(1)).asDefaultResponse }
        return runMas(executor, goal = "say hi to Bob", expected = listOf("failed: say hi to Bob")) {
            failing.goal { this } triggers {
                log += "failed: $context"
                terminateNode()
            }
            llmPlans { goal("greet {name}") { fail("no plan should run") } }
        }
    }

    @Test
    fun `added beliefs trigger belief plans`(): TestResult {
        val executor = getMockExecutor {
            mockLLMAnswer(choice(1, "degrees" to "30")) onRequestContains "belief added"
        }
        val expected = listOf("it is 30 degrees outside -> 30")
        return runMas(executor, goal = "check the thermometer", expected = expected) {
            adding.goal { takeIf { it == "check the thermometer" } } triggers {
                agent.believe("it is 30 degrees outside")
            }
            llmPlans {
                belief("the temperature is {degrees} degrees") {
                    log += "${context.event} -> ${context["degrees"]}"
                    terminateNode()
                }
            }
        }
    }

    @Test
    fun `tests query the beliefs and revisions replace contradicted beliefs`(): TestResult {
        val executor = getMockExecutor {
            mockLLMAnswer(choice(1, "state" to "closed")) onRequestContains "\"the door is {state}\""
            mockLLMAnswer(choice(0)) onRequestContains "\"the window is open\""
            mockLLMAnswer("""{"reason": "scripted", "contradicted": [1]}""") onRequestContains "New belief"
        }
        val beliefs = listOf("the door is closed", "the light is on")
        val expected = listOf("{state=closed}", "null", "[the light is on, the door is open]")
        return runMas(executor, beliefs, goal = "go", expected = expected) {
            adding.goal { takeIf { it == "go" } } triggers {
                log += test("the door is {state}").toString()
                log += test("the window is open").toString()
                revise("the door is open")
                log += agent.beliefs.toList().toString()
                terminateNode()
            }
        }
    }

    @Test
    fun `the call budget stops a goal that the LLM keeps sending back to the same plan`(): TestResult {
        val executor = getMockExecutor { mockLLMAnswer(choice(1)).asDefaultResponse }
        return runMas(executor, goal = "loop", expected = listOf("failed: loop"), maxCalls = 3) {
            failing.goal { this } triggers {
                log += "failed: $context"
                terminateNode()
            }
            llmPlans { goal("loop") { agent.achieve(context.event) } }
        }
    }
}
