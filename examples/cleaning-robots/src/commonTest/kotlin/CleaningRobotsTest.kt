import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest
import model.Mars
import model.MarsState
import model.Pos

class CleaningRobotsTest {
    @Test
    fun robotsCleanEveryGarbageAndStop() = runTest {
        val garbage = setOf(Pos(0, 0), Pos(4, 1), Pos(2, 4), Pos(4, 4))
        val mars = Mars(MarsState(size = 5, garbage = garbage), Random(1))

        mas(NodeBuilders.baseNode()) {
            marsNode(mars) { Duration.ZERO }
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))

        val end = mars.state.value
        assertEquals(emptySet(), end.garbage)
        assertEquals(false, end.r1Carrying)
        assertEquals(Pos(4, 4), end.r1)
    }
}
