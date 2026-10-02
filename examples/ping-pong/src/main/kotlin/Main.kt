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

private typealias Message = Pair<String, AgentID>

private val alice = BaseAgentID("Alice")
private val bob = BaseAgentID("Bob")

private fun AgentBuilder<Message, String, Any>.receivesTextMessages() {
    embodiedAs { Any() }
    handlesMessageEvents { message ->
        when (val payload = message.payload) {
            is String -> AgentUpdate.Belief(setOf(payload to message.sender), emptySet())
            else -> null
        }
    }
}

private val ponger = agent<Message, String, Any>(bob) {
    receivesTextMessages()
    context(MessagingSkill(node)) {
        hasPlanLibrary {
            adding.belief {
                takeIf { it.first == "Ping!" }
            } triggers {
                val (text, sender) = context
                agent.print("Received \"$text\" from ${sender.displayName}")
                agent.sendTo(sender, "Pong!")
                node.terminateNode()
            }
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
            withAgents(ponger)
        }
        node {
            context(MessagingSkill(node)) {
                agent<Message, String>(alice) {
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
                            takeIf { it.first == "Pong!" }
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
