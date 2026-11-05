import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.Pos
import model.ROBOT_STARTS
import model.VacuumWorld
import model.WorldState
import model.defaultMap

class VacuumRobotTest {
    /**
     * Lets [robots] robots clean [world] until no dust is left, or it has taken too long; the robots never stop.
     * Time is virtual: the world's seconds pass instantly.
     */
    private suspend fun cleanUp(world: VacuumWorld, robots: Int = 1): Pair<WorldState, List<VacuumBody>> =
        coroutineScope {
            val bodies = ROBOT_STARTS.take(robots).map { VacuumBody(world, it.position, it.facing) }
            val run = launch { runVacuumWorld(world, bodies) }
            val end = world.state.first { it.dust.isEmpty() || it.time > MAX_SECONDS }
            run.cancel()
            end to bodies
        }

    @Test
    fun robotFindsAndCleansAllTheDust() = runTest {
        val (end, bodies) = cleanUp(VacuumWorld(defaultMap(dustCount = 10, random = Random(1))))
        assertEquals(emptySet(), end.dust, "Dust left after ${end.time} seconds")
        assertEquals(10, bodies.single().state.value.cleaned)
    }

    @Test
    fun robotGoesBackToTheDustItRemembers() = runTest {
        // at the start the robot, facing east, sees dust ahead and on its right: it cleans the first, then the other
        val (end, _) = cleanUp(VacuumWorld(defaultMap(dustCount = 0).copy(dust = setOf(Pos(1, 0), Pos(0, 1)))))
        // forward, clean, then back to (0, 0) and down to (0, 1), clean: about 12 seconds, no exploring
        assertTrue(end.time <= REMEMBERED_DUST_SECONDS, "Both cleaned only after ${end.time} seconds")
    }

    @Test
    fun robotsCleanTogetherWithoutCrashing() = runTest {
        val (end, bodies) = cleanUp(VacuumWorld(defaultMap(dustCount = 10, random = Random(1))), robots = 4)
        assertEquals(emptySet(), end.dust, "Dust left after ${end.time} seconds")
        assertEquals(10, bodies.sumOf { it.state.value.cleaned })
        assertEquals(bodies.size, bodies.map { it.state.value.position }.toSet().size, "Two robots on one cell")
    }

    private companion object {
        const val MAX_SECONDS = 3000
        const val REMEMBERED_DUST_SECONDS = 14
    }
}
