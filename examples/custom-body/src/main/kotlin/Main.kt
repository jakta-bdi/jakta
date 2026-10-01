import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import kotlinx.coroutines.runBlocking

private class Robot(val name: String) {
    var x = 0
    var y = 0
}

private data class Moved(val x: Int, val y: Int) : Perception

private class GridMovement(private val node: Node<Robot>) {
    fun Agent.moveTo(x: Int, y: Int) {
        val body = node.agents.getValue(id)
        body.x = x
        body.y = y
        // only the robot that moved perceives its new position
        node.publishEvent(Moved(x, y)) { it === body }
    }
}

context(movement: GridMovement)
private fun Agent.moveTo(x: Int, y: Int) = with(movement) { moveTo(x, y) }

/**
 * A robot patrolling the grid.
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Assert)
    mas(NodeBuilders.baseNode<Robot>()) {
        node {
            context(GridMovement(node)) {
                agent<String, String>(BaseAgentID("R2")) {
                    embodiedAs { id -> Robot(id.displayName) }
                    handlesPerceptionEvents { perception ->
                        when (perception) {
                            is Moved -> AgentUpdate.Belief(
                                setOf("at(${perception.x},${perception.y})"),
                                beliefs.filter { it.startsWith("at(") }.toSet(),
                            )

                            else -> null
                        }
                    }
                    hasInitialGoals { !"patrol" }
                    hasPlanLibrary {
                        adding.goal {
                            takeIf { it == "patrol" }
                        } triggers {
                            agent.moveTo(1, 0)
                            agent.moveTo(1, 1)
                        }
                        adding.belief {
                            takeIf { it == "at(1,1)" }
                        } triggers {
                            val body = node.agents.getValue(agent.id)
                            agent.print("${body.name} reached (${body.x}, ${body.y})")
                            node.terminateNode()
                        }
                    }
                }
            }
        }
    }.runLocally()
}
