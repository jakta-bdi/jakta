import it.unibo.jakta.node.Node
import kotlin.math.abs
import model.Block
import model.Stacks

/**
 * The DEF names of the boxes in `worlds/blocksworld.wbt`, which are also the names of the blocks.
 */
val BLOCKS = listOf("A", "B", "C")

/**
 * Where the stacks can stand, as metres to the left of the robot; at least one more slot than blocks, so that the
 * "table" always has room.
 */
val SLOTS = listOf(0.09, 0.03, -0.03, -0.09)

/**
 * How far ahead of the robot's centre, at the world's origin facing x, the slots are: the first arm joint is 0.189
 * metres ahead of the centre.
 */
const val SLOT_DISTANCE = 0.189 + ARM_REACH

private const val TOLERANCE = 0.025

/**
 * The stack on each slot, bottom first, given where the boxes are; a box off every slot, e.g. fallen on the floor,
 * is in no stack.
 */
fun stacksFrom(positions: Map<String, DoubleArray>): List<List<Block>> = SLOTS.map { slot ->
    positions.filterValues { (x, y, _) -> abs(x - SLOT_DISTANCE) < TOLERANCE && abs(y - slot) < TOLERANCE }
        .entries
        .sortedBy { (_, position) -> position[2] }
        .map { (name, _) -> Block(name) }
}

/**
 * The Blocks World skills on a youBot in Webots: moves are arm motions, and the agent perceives the stacks where the
 * boxes are, whenever they change, even when the arm is not the cause.
 */
class WebotsBlocksWorldSkills(private val webots: Webots, private val node: Node<*>) : BlocksWorldSkills {

    @Volatile
    private var perceived: Stacks? = null

    init {
        webots.onIdle = { perceive(stacksFrom(positions(BLOCKS))) }
    }

    private fun perceive(slots: List<List<Block>>, always: Boolean = false) {
        val stacks = slots.filter { it.isNotEmpty() }
        if (always || stacks != perceived) {
            perceived = stacks
            node.publishEvent(BlocksWorldPerception(stacks))
        }
    }

    override suspend fun join() {
        perceive(webots.run { stacksFrom(positions(BLOCKS)) }, always = true)
    }

    override suspend fun move(block: String, destination: String) {
        val slots = webots.run {
            val slots = stacksFrom(positions(BLOCKS))
            val from = slots.indexOfFirst { it.lastOrNull()?.id == block }
            require(from >= 0) { "$block is not on top of a stack the arm can reach" }
            val to = if (destination == "table") {
                slots.indexOfFirst { it.isEmpty() }
            } else {
                slots.indexOfFirst { it.lastOrNull()?.id == destination }
            }
            require(to >= 0) { "there is no room to put $block on $destination" }
            gripBox(SLOTS[from], level = slots[from].size - 1, grip = true)
            gripBox(SLOTS[to], level = slots[to].size, grip = false)
            wait(1.0) // let the box settle
            stacksFrom(positions(BLOCKS))
        }
        perceive(slots)
        val landed = slots.any { stack ->
            val i = stack.indexOf(Block(block))
            i >= 0 && if (destination == "table") i == 0 else i > 0 && stack[i - 1].id == destination
        }
        check(landed) { "$block did not land on $destination" }
    }
}
