import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.VacuumWorld
import model.defaultWorld

class VacuumRobotTest {
    @Test
    fun robotFindsAndCleansAllTheDust() = runTest {
        val world = VacuumWorld(defaultWorld(dustCount = 10, random = Random(1)))
        val robot = launch {
            mas(NodeBuilders.baseNode()) {
                vacuumNode(world, stepTime = { Duration.ZERO }, dustChance = { 0.0 })
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }
        // the robot never stops: wait until the map is clean, or it has taken too long
        val end = world.state.first { it.dust.isEmpty() || it.time > MAX_STEPS }
        robot.cancel()
        assertEquals(emptySet(), end.dust, "Dust left after ${end.time} steps")
        assertEquals(10, end.cleaned)
    }

    private companion object {
        const val MAX_STEPS = 1000
    }
}
