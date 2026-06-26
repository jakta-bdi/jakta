package model

import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents a block in the Blocks World.
 *
 * @property id The unique identifier of the block.
 */
data class Block(val id: String)

/**
 * Represents the Blocks World environment, which consists of stacks of blocks.
 *
 * @property seed The seed for the random number generator used to initialize the world.
 * @property blockCount The number of blocks in the world.
 */
class BlocksWorld(seed: Long = 42, blockCount: Int = 6) {

    private val random = Random(seed)
    private val mutex = Mutex()

    private val stacks: MutableList<MutableList<Block>> = mutableListOf()

    init {
        initialize(blockCount)
    }

    // ----------------------------
    // Public API (coroutine-safe)
    // ----------------------------

    /**
     * Moves a block from its current stack to the destination stack.
     */
    suspend fun move(block: Block, destination: Block?): List<List<Block>> = mutex.withLock {
        val fromStackIndex = checkNotNull(findStackIndex(block)) { "model.Block $block not found" }

        val fromStack = stacks[fromStackIndex]

        check(!isTop(fromStack, block)) {
            "model.Block $block is not clear"
        }

        val moving = fromStack.removeAt(fromStack.lastIndex)
        if (fromStack.isEmpty()) stacks.removeAt(fromStackIndex)

        if (destination == null) {
            stacks.add(mutableListOf(moving))
            return getStateUnsafe()
        }

        val destStackIndex = checkNotNull(findStackIndex(destination)) {
            "Destination $destination not found"
        }

        val destStack = stacks[destStackIndex]

        check(!isTop(destStack, destination)) {
            "Destination $destination is not clear"
        }

        delay(1.seconds)
        destStack.add(moving)
        return getStateUnsafe()
    }

    /**
     * Returns the current state of the Blocks World as a list of stacks of blocks.
     */
    suspend fun getState(): List<List<Block>> = mutex.withLock {
        getStateUnsafe()
    }

    /**
     * Prints the current state of the Blocks World to the console.
     */
    suspend fun printState() = mutex.withLock {
        stacks.forEachIndexed { i, stack ->
            println("Stack $i: ${stack.joinToString(" ")}")
        }
    }

    // ----------------------------
    // Initialization
    // ----------------------------

    private fun initialize(blockCount: Int) {
        val blocks = ('A' until ('A' + blockCount)).map { Block(it.toString()) }.toList()

        blocks.forEach { stacks.add(mutableListOf(it)) }

        repeat(blockCount * 2) {
            if (stacks.size < 2) return@repeat

            val from = random.nextInt(stacks.size)
            val to = random.nextInt(stacks.size)

            if (from != to && stacks[from].isNotEmpty()) {
                val block = stacks[from].removeAt(stacks[from].lastIndex)
                stacks[to].add(block)

                if (stacks[from].isEmpty()) {
                    stacks.removeAt(from)
                }
            }
        }
    }

    // ----------------------------
    // Internal helpers (lock-protected by caller)
    // ----------------------------

    private fun getStateUnsafe(): List<List<Block>> = stacks.map { it.toList() }

    private fun findStackIndex(block: Block): Int? = stacks.indexOfFirst { it.contains(block) }
        .takeIf { it >= 0 }

    private fun isTop(stack: List<Block>, block: Block): Boolean = stack.lastOrNull() == block
}
