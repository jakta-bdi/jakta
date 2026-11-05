package it.unibo.jakta.dsl.examples

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

data class Reading(val value: Int) : Perception

class TestNodeProcesses {

    @OptIn(ExperimentalCoroutinesApi::class) // currentTime
    @Test
    fun processesRunAlongsideAgentsUntilTheNodeTerminates() = runTest {
        Logger.setMinSeverity(Severity.Error)
        val readings = mutableListOf<Pair<Int, Long>>()
        var polls = 0
        mas(NodeBuilders.baseNode<Any>()) {
            node {
                // the environment polls a sensor every second, independently of the agents
                node.launchProcess {
                    while (true) {
                        delay(1.seconds)
                        polls++
                        node.publishEvent(Reading(polls))
                    }
                }
                agent<String, String>(BaseAgentID("monitor")) {
                    embodiedAs { Any() }
                    handlesPerceptionEvents { (it as? Reading)?.let { r -> AgentUpdate.Belief(setOf("${r.value}")) } }
                    hasPlanLibrary {
                        adding.belief { toIntOrNull() } triggers {
                            readings += context to testScheduler.currentTime
                            if (context == 3) node.terminateNode()
                        }
                    }
                }
            }
        }.runLocally()
        assertEquals(listOf(1 to 1000L, 2 to 2000L, 3 to 3000L), readings)
        delay(10.seconds)
        assertEquals(3, polls, "the process is cancelled with the node")
    }
}
