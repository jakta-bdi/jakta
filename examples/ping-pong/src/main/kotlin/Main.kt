import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlinx.coroutines.runBlocking

private data class ToldBelief(val text: String, val sender: AgentID)

private val alice = BaseAgentID("Alice")
private val bob = BaseAgentID("Bob")

private fun AgentBuilder<ToldBelief, String, Any>.receivesTextMessages() {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        when (val payload = message.payload) {
            is String -> AgentUpdate.Belief(setOf(ToldBelief(payload, message.sender)), emptySet())
            else -> null
        }
    }
}

context(_: MessagingSkill)
private fun ponger() = agent<ToldBelief, String, Any>(bob) {
    receivesTextMessages()
    hasPlanLibrary {
        adding.belief {
            takeIf { it.text == "Ping!" }
        } triggers {
            val (text, sender) = context
            agent.print("Received \"$text\" from ${sender.displayName}")
            agent.sendTo(sender, "Pong!")
            node.terminateNode()
        }
    }
}

/**
 * Alice pings Bob, who lives on another node, and Bob answers with a pong.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            context(MessagingSkill(node)) {
                withAgents(ponger())
            }
        }
        node {
            context(MessagingSkill(node)) {
                agent<ToldBelief, String>(alice) {
                    receivesTextMessages()
                    hasInitialGoals {
                        !"sendPing"
                    }
                    hasPlanLibrary {
                        adding.goal {
                            takeIf { it == "sendPing" }
                        } triggers {
                            agent.sendTo(bob, "Ping!")
                        }
                        adding.belief {
                            takeIf { it.text == "Pong!" }
                        } triggers {
                            agent.print("Got the pong, stopping.")
                            node.terminateNode()
                        }
                    }
                }
            }
        }
    }.runLocally()
}
