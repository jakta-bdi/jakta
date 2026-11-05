import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import model.VacuumWorld

/**
 * Runs the robots of [bodies] in their [world] until cancelled: the world runs on its own clock, and the agents act
 * through their bodies.
 */
suspend fun runVacuumWorld(world: VacuumWorld, bodies: List<VacuumBody>): Unit = coroutineScope {
    launch { world.simulate() }
    mas(NodeBuilders.baseNode()) { withNodes(vacuumNode(bodies)) }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
}
