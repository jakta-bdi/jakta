package model

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

/**
 * Represents a block in the Blocks World.
 *
 * @property id The unique identifier of the block.
 */
data class Block(val id: String)

/**
 * Stacks of blocks, each listed bottom first.
 */
typealias Stacks = List<List<Block>>

/**
 * Represents the Blocks World environment, which consists of stacks of blocks.
 *
 * @param initial the initial stacks of blocks.
 */
class BlocksWorld(initial: Stacks) {

    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the Blocks World.
     */
    val state: StateFlow<Stacks> = mutableState.asStateFlow()

    /**
     * How long a single move of the agent takes, to make its work visible.
     */
    var moveDelay: Duration = 1.seconds

    /**
     * Moves a block on top of [destination], or on the table if [destination] is null, taking [moveDelay].
     * Returns the new state of the world.
     */
    suspend fun move(block: Block, destination: Block?): Stacks {
        delay(moveDelay)
        return mutableState.updateAndGet { it.moved(block, destination) }
    }

    /**
     * Instantly moves a block, e.g. when the user rearranges the world.
     */
    fun rearrange(block: Block, destination: Block?) {
        mutableState.update { it.moved(block, destination) }
    }
}

/**
 * Returns these stacks with [block] moved on top of [destination], or on the table if [destination] is null.
 */
fun Stacks.moved(block: Block, destination: Block?): Stacks {
    val from = indexOfFirst { block in it }
    require(from >= 0) { "Block $block not found" }
    require(this[from].last() == block) { "Block $block is not clear" }
    val to = destination?.let { dest ->
        indexOfFirst { dest in it }.also {
            require(it >= 0) { "Destination $dest not found" }
            require(this[it].last() == dest) { "Destination $dest is not clear" }
            require(it != from) { "Cannot move $block on itself" }
        }
    }
    if (to == null && this[from].size == 1) return this // already on the table

    val result = map { it.toMutableList() }.toMutableList()
    result[from].removeAt(result[from].lastIndex)
    if (to == null) result.add(mutableListOf(block)) else result[to].add(block)
    return result.filter { it.isNotEmpty() }
}

/**
 * Creates [blockCount] blocks named `A`, `B`, ... and piles them randomly.
 */
fun randomStacks(blockCount: Int, random: Random = Random.Default): Stacks {
    val stacks = ('A' until 'A' + blockCount).map { mutableListOf(Block(it.toString())) }.toMutableList()
    repeat(blockCount * 2) {
        val from = random.nextInt(stacks.size)
        val to = random.nextInt(stacks.size)
        if (stacks.size > 1 && from != to) {
            stacks[to].add(stacks[from].removeAt(stacks[from].lastIndex))
            if (stacks[from].isEmpty()) stacks.removeAt(from)
        }
    }
    return stacks
}
