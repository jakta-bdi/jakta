import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.jakta.skills.MessagingSkill
import it.unibo.jakta.skills.sendTo
import kotlinx.coroutines.runBlocking

private val pinger = BaseAgentID("Pinger")
private val ponger = BaseAgentID("Ponger")

private val pingNode = node(NodeBuilders.baseNode()) {
    context(MessagingSkill(node)) {
        agent<String, String>(pinger) {
            embodiedAs { Any() }
            handlesMessageEvents { message ->
                (message.payload as? String)?.let { AgentUpdate.Belief(setOf(it)) }
            }
            hasInitialGoals { !"ping" }
            hasPlanLibrary {
                adding.goal {
                    takeIf { it == "ping" }
                } triggers {
                    agent.sendTo(ponger, "ping")
                }
                adding.belief {
                    takeIf { it == "pong" }
                } triggers {
                    agent.print("Got pong from the other node")
                    node.terminateNode()
                }
            }
        }
    }
}

private val pongNode = node(NodeBuilders.baseNode()) {
    context(MessagingSkill(node)) {
        agent<Pair<String, AgentID>, String>(ponger) {
            embodiedAs { Any() }
            handlesMessageEvents { message ->
                (message.payload as? String)?.let { AgentUpdate.Belief(setOf(it to message.sender)) }
            }
            hasPlanLibrary {
                adding.belief {
                    takeIf { it.first == "ping" }
                } triggers {
                    agent.print("Got ping, replying")
                    agent.sendTo(context.second, "pong")
                    node.terminateNode()
                }
            }
        }
    }
}

/**
 * Runs the two nodes together.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        withNodes(pingNode, pongNode)
    }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
