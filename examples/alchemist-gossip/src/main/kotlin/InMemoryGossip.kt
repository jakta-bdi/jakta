import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.mas.runLocally
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.situated.Coordinates
import it.unibo.jakta.situated.InMemorySpace
import it.unibo.jakta.situated.SituatedBody
import it.unibo.jakta.skills.MessagingSkill
import kotlinx.coroutines.runBlocking

private const val SIDE = 10
private const val RANGE = 1.5

/**
 * Runs the same gossipers of `gossip.yml` without Alchemist: one node per agent on a 10x10 grid,
 * with the default runner and the situated skills of an [InMemorySpace].
 */
fun main(): Unit = runBlocking {
    Logger.setMinSeverity(Severity.Warn)
    val space = InMemorySpace(RANGE)
    mas(NodeBuilders.baseNode<Any>()) {
        for (x in 0 until SIDE) {
            for (y in 0 until SIDE) {
                node {
                    context(space.skillsFor(node), MessagingSkill(node)) {
                        gossiper(BaseAgentID("agent-$x-$y")) {
                            SituatedBody(Coordinates(x.toDouble(), y.toDouble()), mapOf(SOURCE to (x == 0 && y == 0)))
                        }
                    }
                }
            }
        }
    }.runLocally()
    println("All agents are informed")
}
