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
import model.BlocksWorld

private const val DEFAULT_SEED = 42L
private const val DEFAULT_BLOCK_COUNT = 10
private const val MAX_BLOCK_COUNT = 26

/**
 * State holder for the Blocks World application, managing the goal, world state, and agent execution.
 *
 * @property agentDispatcher where the agent runs, off the UI thread on desktop.
 */
class BlocksWorldAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        Logger.setMinSeverity(Severity.Error)
        Logger.setLogWriters(platformLogWriter(), AgentTrace)
    }

    /**
     * The seed used for initializing the Blocks World random generator.
     */
    var seed by mutableStateOf(DEFAULT_SEED.toString())

    /**
     * The number of blocks in the Blocks World.
     */
    var blockCount by mutableStateOf(DEFAULT_BLOCK_COUNT.toString())

    /**
     * The desired towers, listed top to bottom and separated by `;`, e.g. `[A, B]; [C]`.
     */
    var goalText by mutableStateOf("[A, B]; [C, D, E]; [F]")

    /**
     * The current instance of the BlocksWorld.
     */
    var world by mutableStateOf(BlocksWorld(DEFAULT_SEED, DEFAULT_BLOCK_COUNT))
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
     * Whether the agent is currently running.
     */
    var isRunning by mutableStateOf(false)
        private set

    /**
     * Why [goalText] is not a valid goal for the current world, or null if it is.
     */
    val goalError: String?
        get() {
            val names = parseTowers(goalText).flatten()
            val unknown = names - world.state.value.flatten().map { it.id }.toSet()
            return when {
                names.isEmpty() -> "The goal is empty"
                names.size != names.toSet().size -> "Each block can appear only once"
                unknown.isNotEmpty() -> "Unknown blocks: ${unknown.joinToString()}"
                else -> null
            }
        }

    private var agentJob: Job? = null

    /**
     * Stops the agent and creates a new world from [seed] and [blockCount].
     */
    fun reset() {
        stop()
        val newSeed = seed.toLongOrNull() ?: DEFAULT_SEED
        val newBlockCount = (blockCount.toIntOrNull() ?: DEFAULT_BLOCK_COUNT).coerceIn(1, MAX_BLOCK_COUNT)
        blockCount = newBlockCount.toString()
        world = BlocksWorld(newSeed, newBlockCount).also { it.moveDelay = moveDelay }
    }

    /**
     * Starts the agent, which tries to reach the goal from the current world state.
     */
    fun play(scope: CoroutineScope) {
        if (isRunning || goalError != null) return
        val goal = parseGoal(goalText)
        val currentWorld = world
        AgentTrace.clear()
        isRunning = true
        val job = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                mas(NodeBuilders.baseNode()) {
                    blocksWorldNode(currentWorld, goal)
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            } finally {
                // a stopped run must not flag a newer one as finished
                if (agentJob === coroutineContext.job) isRunning = false
            }
        }
        agentJob = job
        job.start()
    }

    /**
     * Stops the agent if it is currently running.
     */
    fun stop() {
        agentJob?.cancel()
        agentJob = null
        isRunning = false
    }
}

/**
 * Splits a goal like `[A, B]; [C]` into its towers of block names.
 */
fun parseTowers(text: String): kotlin.collections.List<kotlin.collections.List<String>> = text
    .split(";")
    .map { it.trim().removePrefix("[").removeSuffix("]") }
    .filter { it.isNotBlank() }
    .map { tower -> tower.split(",").map { it.trim() }.filter { it.isNotEmpty() } }

/**
 * Parses a goal like `[A, B]; [C]` into the `state/1` goal pursued by the agent.
 */
fun parseGoal(text: String): PrologGoal {
    val towers = parseTowers(text).map { tower -> List.of(tower.map { Atom.of(it) }) }
    return initialGoal { "state"(logicListOf(*towers.toTypedArray())) }
}
