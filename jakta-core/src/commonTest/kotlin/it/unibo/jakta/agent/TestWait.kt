package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
import it.unibo.jakta.event.beliefAdded
import it.unibo.jakta.event.beliefRemoved
import it.unibo.jakta.event.messageReceived
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest

class TestWait {

    @Test
    fun twoWaitsOnTheSameFilterBothResume(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val parcel = beliefAdded<String> { it == "parcel" }
        val received = mutableListOf<String?>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !"first"
                                !"second"
                                !"deliver"
                            }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "first" || it == "second" } } triggers {
                                    received += agent.wait(parcel, 30.seconds)
                                }
                                adding.goal { takeIf { it == "deliver" } } triggers {
                                    delay(1.seconds)
                                    agent.believe("parcel")
                                    delay(1.seconds)
                                    node.terminateNode()
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf<String?>("parcel", "parcel"), received)
        }
    }

    @Test
    fun waitUntilBelieveReturnsRightAwayWhatIsAlreadyBelieved(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val received = mutableListOf<String?>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            believes { +"parcel" }
                            hasInitialGoals { !"check" }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "check" } } triggers {
                                    // waiting for events alone would time out: the belief came before the call
                                    received += agent.waitUntilBelieve(1.seconds) { it == "parcel" }
                                    node.terminateNode()
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf<String?>("parcel"), received)
        }
    }

    @Test
    fun waitUntilBelieveWaitsForAMatchingBelief(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val received = mutableListOf<String?>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !"check"
                                !"deliver"
                            }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "check" } } triggers {
                                    received += agent.waitUntilBelieve(30.seconds) { it.startsWith("parcel") }
                                    received += agent.waitUntilBelieve(1.seconds) { it == "never" }
                                    node.terminateNode()
                                }
                                adding.goal { takeIf { it == "deliver" } } triggers {
                                    delay(1.seconds)
                                    agent.believe("parcel-42")
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf("parcel-42", null), received)
        }
    }

    @Test
    fun filtersMatchOnlyTheirKindOfEventAndType() {
        assertEquals("a", beliefAdded<String>()(BeliefAddEvent("a")))
        assertNull(beliefAdded<String>()(BeliefRemoveEvent("a")))
        assertNull(beliefAdded<Int>()(BeliefAddEvent("a")))
        assertEquals("a", beliefRemoved<String>()(BeliefRemoveEvent("a")))
        val sender = BaseAgentID("sender")
        assertEquals(Message("hi", sender), messageReceived<String>()(Message("hi", sender)))
        assertNull(messageReceived<Int>()(Message("hi", sender)))
        assertNull(messageReceived<String> { it.payload == "bye" }(Message("hi", sender)))
    }
}
