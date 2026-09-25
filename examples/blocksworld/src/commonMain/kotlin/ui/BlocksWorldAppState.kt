package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import blocksWorldNode
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.List
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import model.Block
import model.BlocksWorld
import model.Stacks
import model.moved
import model.randomStacks

private const val DEFAULT_BLOCK_COUNT = 6

/**
 * The fewest blocks the user can choose.
 */
const val MIN_BLOCK_COUNT = 2

/**
 * The most blocks the user can choose.
 */
const val MAX_BLOCK_COUNT = 12

/**
 * State holder for the Blocks World application: the world, the goal the user arranged, and the agent run.
 *
 * @property agentDispatcher where the agent runs, off the UI thread on desktop.
 */
class BlocksWorldAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        Logger.setMinSeverity(Severity.Error)
        Logger.setLogWriters(platformLogWriter(), AgentTrace)
    }

    /**
     * The number of blocks in the world and in the goal.
     */
    var blockCount by mutableStateOf(DEFAULT_BLOCK_COUNT)
        private set

    /**
     * The world the agent acts on.
     */
    var world by mutableStateOf(BlocksWorld(randomStacks(DEFAULT_BLOCK_COUNT)))
        private set

    /**
     * The arrangement the agent has to reach.
     */
    var goal by mutableStateOf(randomStacks(DEFAULT_BLOCK_COUNT))
        private set

    private var moveDelayState by mutableStateOf(1.seconds)

    /**
     * How long each move of the agent takes; it can be changed while the agent runs.
     */
    var moveDelay: Duration
        get() = moveDelayState
        set(value) {
            moveDelayState = value
            world.moveDelay = value
        }

    /**
     * Whether the agent is currently running; the user can edit world and goal only when it is not.
     */
    var isRunning by mutableStateOf(false)
        private set

    private var agentJob: Job? = null

    /**
     * Changes the number of blocks, reshuffling world and goal.
     */
    fun changeBlockCount(count: Int) {
        blockCount = count.coerceIn(MIN_BLOCK_COUNT, MAX_BLOCK_COUNT)
        shuffleWorld()
        shuffleGoal()
    }

    /**
     * Stops the agent, if running, and piles the blocks of the world randomly.
     */
    fun shuffleWorld() {
        agentJob?.cancel()
        agentJob = null
        isRunning = false
        world = BlocksWorld(randomStacks(blockCount)).also { it.moveDelay = moveDelay }
    }

    /**
     * Piles the blocks of the goal randomly.
     */
    fun shuffleGoal() {
        if (!isRunning) goal = randomStacks(blockCount)
    }

    /**
     * Moves a block of the world by hand.
     */
    fun moveInWorld(block: Block, destination: Block?) {
        if (!isRunning) world.rearrange(block, destination)
    }

    /**
     * Moves a block of the goal by hand.
     */
    fun moveInGoal(block: Block, destination: Block?) {
        if (!isRunning) goal = goal.moved(block, destination)
    }

    /**
     * Starts the agent, which works until it reaches the goal (or gives up).
     */
    fun play(scope: CoroutineScope) {
        if (isRunning) return
        val desired = goalOf(goal)
        val currentWorld = world
        AgentTrace.clear()
        isRunning = true
        val job = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                mas(NodeBuilders.baseNode()) {
                    blocksWorldNode(currentWorld, desired)
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            } finally {
                // a cancelled run must not flag a newer one as finished
                if (agentJob === coroutineContext.job) isRunning = false
            }
        }
        agentJob = job
        job.start()
    }
}

/**
 * Whether two arrangements have the same towers, regardless of their order on the table.
 */
fun Stacks.sameTowersAs(other: Stacks): Boolean = toSet() == other.toSet()

/**
 * The `state/1` goal pursued by the agent to reach [stacks]; the agent lists towers top first.
 */
fun goalOf(stacks: Stacks): PrologGoal {
    val towers = stacks.map { stack -> List.of(stack.reversed().map { Atom.of(it.id) }) }
    return initialGoal { "state"(logicListOf(*towers.toTypedArray())) }
}
