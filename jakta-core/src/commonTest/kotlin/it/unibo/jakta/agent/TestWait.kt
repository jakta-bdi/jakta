package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
import it.unibo.jakta.event.beliefAdded
import it.unibo.jakta.event.beliefRemoved
import it.unibo.jakta.event.messageReceived
import it.unibo.jakta.event.perceived
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
                                    received += agent.waitForEvent(parcel, 30.seconds)
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
    fun waitUntilReturnsRightAwayIfTheConditionAlreadyHolds(): TestResult {
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
                                    received += agent.waitUntil(1.seconds) { beliefs.firstOrNull { it == "parcel" } }
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
    fun waitUntilWaitsForTheConditionToHold(): TestResult {
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
                                    received += agent.waitUntil(30.seconds) {
                                        beliefs.firstOrNull { it.startsWith("parcel") }
                                    }
                                    received += agent.waitUntil(1.seconds) { beliefs.firstOrNull { it == "never" } }
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
    fun waitUntilChecksTheWholeState(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val received = mutableListOf<Int?>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            believes { +"answer-1" }
                            hasInitialGoals {
                                !"collect"
                                !"answer"
                            }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "collect" } } triggers {
                                    received += agent.waitUntil(30.seconds) {
                                        beliefs.count { it.startsWith("answer") }.takeIf { it >= 3 }
                                    }
                                    node.terminateNode()
                                }
                                adding.goal { takeIf { it == "answer" } } triggers {
                                    delay(1.seconds)
                                    agent.believe("answer-2")
                                    delay(1.seconds)
                                    agent.believe("answer-3")
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf<Int?>(3), received)
        }
    }

    @Test
    fun waitersSeeTheStateTheEventProduced(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val received = mutableListOf<Int?>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            hasInitialGoals { !"watch" }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "watch" } } triggers {
                                    agent.alsoAchieve("sleep")
                                    // resumed after the goal is handled: its intention is already there
                                    received += agent.waitForEvent({ event ->
                                        if (event is AgentEvent.Internal.Goal.Add<*, *>) agent.intentions.size else null
                                    }, 30.seconds)
                                    node.terminateNode()
                                }
                                adding.goal { takeIf { it == "sleep" } } triggers {
                                    delay(10.seconds)
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf<Int?>(2), received)
        }
    }

    @Test
    fun stepsAreNotEvents(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val received = mutableListOf<AgentEvent?>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            hasInitialGoals {
                                !"watch"
                                !"tick"
                            }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "watch" } } triggers {
                                    received += agent.waitForEvent({ it as? AgentEvent.Internal.Step }, 5.seconds)
                                    node.terminateNode()
                                }
                                adding.goal { takeIf { it == "tick" } } triggers {
                                    repeat(3) { delay(1.seconds) } // each resumption is a step
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf<AgentEvent?>(null), received)
        }
    }

    @Test
    fun signalsAreCaughtWithoutBecomingBeliefs(): TestResult {
        Logger.setMinSeverity(Severity.Assert)
        val received = mutableListOf<Signal?>()
        val beliefs = mutableListOf<Collection<String>>()
        return runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(
                        agent<String, String, Any> {
                            embodiedAs { Any() }
                            handlesPerceptionEvents { null } // a signal: no belief, no goal
                            hasInitialGoals {
                                !"watch"
                                !"signal"
                            }
                            hasPlanLibrary {
                                adding.goal { takeIf { it == "watch" } } triggers {
                                    received += agent.waitForEvent(perceived<Signal>(), 30.seconds)
                                    beliefs += agent.beliefs
                                    node.terminateNode()
                                }
                                adding.goal { takeIf { it == "signal" } } triggers {
                                    delay(1.seconds)
                                    node.publishEvent(Signal)
                                }
                            }
                        },
                    )
                }
            }.runLocally()
            assertEquals(listOf<Signal?>(Signal), received)
            assertEquals(listOf<Collection<String>>(emptySet()), beliefs)
        }
    }

    private data object Signal : AgentEvent.External.Perception

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
