import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

private fun beliefAdded(expected: String): (AgentEvent) -> String? = { event ->
    (event as? AgentEvent.Internal.Belief.Add<*>)?.belief?.takeIf { it == expected } as String?
}

/**
 * An agent waiting for a parcel while another intention delivers it.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            agent<String, String> {
                embodiedAs { Any() }
                hasInitialGoals {
                    !"waitForDelivery"
                    !"deliver"
                }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "waitForDelivery" }
                    } triggers {
                        agent.print("Waiting for the parcel...")
                        val parcel = agent.wait(beliefAdded("parcel"), timeout = 5.seconds)
                        if (parcel != null) agent.print("Parcel received!") else agent.print("Gave up waiting")
                        agent.alsoAchieve("celebrate")
                        agent.achieve("tidyUp")
                        agent.print("Tidied up")
                        node.terminateNode()
                    }
                    adding.goal {
                        takeIf { it == "deliver" }
                    } triggers {
                        delay(2.seconds)
                        agent.believe("parcel")
                    }
                    adding.goal {
                        takeIf { it == "celebrate" }
                    } triggers {
                        agent.print("Celebrating, concurrently")
                    }
                    adding.goal {
                        takeIf { it == "tidyUp" }
                    } triggers {
                        delay(1.seconds)
                    }
                }
            }
        }
    }.runLocally()
}
