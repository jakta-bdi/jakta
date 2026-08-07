package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
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
import kotlinx.coroutines.test.runTest

class TestAgentAdditionAndRemoval {

    val aliceID = BaseAgentID("Alice")

    val alice = agent<String, String, Any>(aliceID) {
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
    fun testAgentAdditionOnSameNode() {
        runTest {
            val mas = mas(NodeBuilders.baseNode()) {
                node {
                    agent(BaseAgentID("Creator")) {
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
    }

    @Test
    fun testAgentRemovalOnSameNode() {
        runTest {
            val mas = mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(alice)
                    agent(BaseAgentID("Destroyer")) {
                        embodiedAs { Any() }
                        hasInitialGoals { !"destroy" }
                        hasPlanLibrary {
                            adding.goal {
                                ifGoalMatch("destroy")
                            } triggers {
                                agent.print("Destroying Alice...")
                                node.removeAgent(aliceID)
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
}
