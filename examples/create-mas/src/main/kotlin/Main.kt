import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import kotlinx.coroutines.runBlocking

private fun greeter(name: String) = agent<String, String, Any>(BaseAgentID(name)) {
    embodiedAs { Any() }
    hasInitialGoals { !"greet" }
    hasPlanLibrary {
        adding.goal {
            takeIf { it == "greet" }
        } triggers {
            agent.print("Hello from $name")
        }
    }
}

private val agentA = greeter("agentA")
private val agentB = greeter("agentB")

/**
 * A MAS with one node and three agents.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode()) {
        node {
            withAgents(agentA, agentB)
            agent<String, String>(BaseAgentID("agentC")) {
                embodiedAs { Any() }
                hasInitialGoals { !"stop" }
                hasPlanLibrary {
                    adding.goal {
                        takeIf { it == "stop" }
                    } triggers {
                        agent.print("Stopping the node")
                        node.terminateNode()
                    }
                }
            }
        }
    }.runLocally()
}
