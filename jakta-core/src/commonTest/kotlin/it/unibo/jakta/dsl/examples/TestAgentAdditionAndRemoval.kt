package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestAgentAdditionAndRemoval {

    val alice = agent<String, String, Any>("Alice") {
        embodiedAs { Any() }
        hasInitialGoals { !"greet" }
        hasPlanLibrary {
            adding.goal {
                ifGoalMatch("greet")
            } triggers {
                agent.print("Hello from Alice!")
            }
        }
    }

    @BeforeTest
    fun setup() {
        Logger.setMinSeverity(Severity.Assert)
    }

    @Test
    fun testAgentAdditionOnSameNode(): TestResult = runTest {
        val mas = mas(NodeBuilders.baseNode()) {
            node {
                agent("Creator") {
                    embodiedAs { Any() }
                    hasInitialGoals { !"create" }
                    hasPlanLibrary {
                        adding.goal {
                            ifGoalMatch("create")
                        } triggers {
                            agent.print("Creating Alice...")
                            node.addAgent(alice)
                            delay(3.seconds)
                            agent.print("Alice created.")
                            agent.print(node.agents.keys.joinToString(", "))
                            node.terminateNode()
                        }
                    }
                }
            }
        }

        mas.run(CoroutineNodeRunner(SharedMemoryNetwork()))
    }

    @Test
    fun testAgentRemovalOnSameNode(): TestResult = runTest {
        val mas = mas(NodeBuilders.baseNode()) {
            node {
                withAgents(alice)
                agent("Destroyer") {
                    embodiedAs { Any() }
                    hasInitialGoals { !"destroy" }
                    hasPlanLibrary {
                        adding.goal {
                            ifGoalMatch("destroy")
                        } triggers {
                            agent.print("Destroying Alice...")
                            node.removeAgent(node.agents.keys.single { it.name == "Alice" })
                            agent.print("Alice destroyed.")
                            delay(3.seconds)
                            agent.print(node.agents.keys.joinToString(", "))
                            node.terminateNode()
                        }
                    }
                }
            }
        }
        mas.run(CoroutineNodeRunner(SharedMemoryNetwork()))
    }
}
