import kotlin.random.Random
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class BlocksWorld(seed: Long = 42, blockCount: Int = 6) {

    private val random = Random(seed)
    private val mutex = Mutex()

    private val stacks: MutableList<MutableList<Char>> = mutableListOf()

    init {
        initialize(blockCount)
    }

    // ----------------------------
    // Public API (coroutine-safe)
    // ----------------------------

    suspend fun move(block: Char, destination: Char?): List<List<Char>> = mutex.withLock {
        val fromStackIndex = findStackIndex(block)
            ?: throw IllegalStateException("Block $block not found")

        val fromStack = stacks[fromStackIndex]

        if (!isTop(fromStack, block)) {
            throw IllegalStateException("Block $block is not clear")
        }

        val moving = fromStack.removeAt(fromStack.lastIndex)
        if (fromStack.isEmpty()) stacks.removeAt(fromStackIndex)

        if (destination == null) {
            stacks.add(mutableListOf(moving))
            return getStateUnsafe()
        }

        val destStackIndex = findStackIndex(destination)
            ?: throw IllegalStateException("Destination $destination not found")

        val destStack = stacks[destStackIndex]

        if (!isTop(destStack, destination)) {
            throw IllegalStateException("Destination $destination is not clear")
        }

        destStack.add(moving)
        return getStateUnsafe()
    }

    suspend fun getState(): List<List<Char>> = mutex.withLock {
        getStateUnsafe()
    }

    suspend fun printState() = mutex.withLock {
        stacks.forEachIndexed { i, stack ->
            println("Stack $i: ${stack.joinToString(" ")}")
        }
    }

    // ----------------------------
    // Initialization
    // ----------------------------

    private fun initialize(blockCount: Int) {
        val blocks = ('A' until ('A' + blockCount)).toList()

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

    fun getStateUnsafe(): List<List<Char>> = stacks.map { it.toList() }

    private fun findStackIndex(block: Char): Int? = stacks.indexOfFirst { it.contains(block) }
        .takeIf { it >= 0 }

    private fun isTop(stack: List<Char>, block: Char): Boolean = stack.lastOrNull() == block
}
