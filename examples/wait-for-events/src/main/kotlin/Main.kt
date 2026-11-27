import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.beliefAdded
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

/**
 * An agent waiting for a parcel while another intention delivers it.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode<Any>()) {
        node {
            agent<String, String> {
                hasInitialGoals {
                    !"waitForDelivery"
                    !"deliver"
                }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "waitForDelivery" }
                    } triggers {
                        agent.print("Waiting for the parcel...")
                        val parcel = agent.waitForEvent(beliefAdded<String> { it == "parcel" }, timeout = 5.seconds)
                        if (parcel != null) agent.print("Parcel received!") else agent.print("Gave up waiting")
                        node.terminateNode()
                    }
                    adding.goal {
                        takeIf { it == "deliver" }
                    } triggers {
                        delay(2.seconds)
                        agent.believe("parcel")
                    }
                }
            }
        }
    }.runLocally()
}
