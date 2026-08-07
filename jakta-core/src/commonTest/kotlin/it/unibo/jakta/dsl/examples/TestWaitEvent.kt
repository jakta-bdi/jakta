package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.ifGoalMatch
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class TestWaitEvent {

    fun filterBeliefEvent(event: AgentEvent): String? = (event as? AgentEvent.Internal.Belief<*>)
        ?.belief
        ?.let { it as? String }
        ?.takeIf { it == "newBelief" }

    fun waitingAgent(timeout: Duration?) = agent {
        embodiedAs { Any() }
        hasInitialGoals {
            !"goal"
            !"believe"
        }
        hasPlanLibrary {
            adding.goal {
                ifGoalMatch("goal")
            } triggers {
                agent.print("I am waiting for a belief to appear")
                val belief = agent.wait<String>({ e -> filterBeliefEvent(e) }, timeout)
                belief?.let {
                    agent.print("Received \"$belief\"")
                } ?: agent.print("Time has run out!")
                node.terminateNode()
            }
            adding.goal {
                ifGoalMatch("believe")
            } triggers {
                delay(10.seconds)
                agent.print("I am adding a belief")
                agent.believe("newBelief")
            }
        }
    }

    @Test
    fun testWaitEvent() {
        Logger.setMinSeverity(Severity.Assert)
        runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(waitingAgent(null))
                }
            }.runLocally()
        }
    }

    @Test
    fun testWaitEventWithTimeout() {
        Logger.setMinSeverity(Severity.Assert)
        runTest {
            mas(NodeBuilders.baseNode()) {
                node {
                    withAgents(waitingAgent(5.seconds))
                }
            }.runLocally()
        }
    }
}
