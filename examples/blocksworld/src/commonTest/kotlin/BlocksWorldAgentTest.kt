import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlinx.coroutines.test.runTest
import model.Block
import model.BlocksWorld
import model.randomStacks
import ui.goalOf

class BlocksWorldAgentTest {
    @Test
    fun agentBuildsTheRequestedTowers() = runTest {
        val world = BlocksWorld(randomStacks(blockCount = 6, Random(42))).apply { moveDelay = Duration.ZERO }
        val goal = listOf(listOf("B", "A"), listOf("E", "D", "C"), listOf("F")).map { it.map(::Block) }

        mas(NodeBuilders.baseNode()) {
            blocksWorldNode(world, goalOf(goal))
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))

        assertEquals(goal.toSet(), world.state.value.toSet())
    }
}
