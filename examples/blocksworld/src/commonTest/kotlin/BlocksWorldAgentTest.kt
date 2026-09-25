import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest
import model.BlocksWorld
import ui.parseGoal

class BlocksWorldAgentTest {
    @Test
    fun agentBuildsTheRequestedTowers() = runTest {
        val world = BlocksWorld(seed = 42, blockCount = 6).apply { moveDelay = Duration.ZERO }

        mas(NodeBuilders.baseNode()) {
            blocksWorldNode(world, parseGoal("[A, B]; [C, D, E]; [F]"))
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))

        // stacks are listed bottom first, goal towers top first
        val towers = world.state.value.map { stack -> stack.map { it.id } }.toSet()
        assertEquals(setOf(listOf("B", "A"), listOf("E", "D", "C"), listOf("F")), towers)
    }
}
