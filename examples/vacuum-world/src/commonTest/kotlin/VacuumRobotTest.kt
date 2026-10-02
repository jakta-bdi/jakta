import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.Pos
import model.VacuumState
import model.VacuumWorld
import model.defaultWorld

class VacuumRobotTest {
    /**
     * Lets the robot clean [world] until no dust is left, or it has taken too long; the robot itself never stops.
     */
    private suspend fun cleanUp(world: VacuumWorld): Pair<VacuumState, VacuumBody> = coroutineScope {
        val body = VacuumBody(world)
        val robot = launch {
            mas(NodeBuilders.baseNode<VacuumBody>()) {
                vacuumNode(body, stepTime = { Duration.ZERO }, dustChance = { 0.0 })
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }
        val end = world.state.first { it.dust.isEmpty() || it.time > MAX_STEPS }
        robot.cancel()
        end to body
    }

    @Test
    fun robotFindsAndCleansAllTheDust() = runTest {
        val (end, body) = cleanUp(VacuumWorld(defaultWorld(dustCount = 10, random = Random(1))))
        assertEquals(emptySet(), end.dust, "Dust left after ${end.time} steps")
        assertEquals(10, body.state.value.cleaned)
    }

    @Test
    fun robotGoesBackToTheDustItRemembers() = runTest {
        // at the start the robot, facing east, sees dust ahead and on its right: it cleans the first, then the other
        val (end, _) = cleanUp(VacuumWorld(defaultWorld(dustCount = 0).copy(dust = setOf(Pos(1, 0), Pos(0, 1)))))
        // forward, clean, then back through (0, 0) and down to (0, 1): a few turns and moves, no exploring
        assertTrue(end.time <= REMEMBERED_DUST_STEPS, "Both cleaned only after ${end.time} steps")
    }

    private companion object {
        const val MAX_STEPS = 1000
        const val REMEMBERED_DUST_STEPS = 10
    }
}
