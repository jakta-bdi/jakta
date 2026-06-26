package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import blocksWorldNode
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.mas.mas
import it.unibo.jakta.dsl.node.LocalNodeBuilder
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.List
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.BlocksWorld

/**
 * ViewModel for the Blocks World application.
 *
 * @property world The BlocksWorld instance representing the current state of the world.
 */
class BlocksWorldViewModel(private val world: BlocksWorld) {

    private val _state = MutableStateFlow(runBlocking { world.getState() })

    /**
     * A read-only StateFlow representing the current state of the Blocks World.
     */
    val state = _state.asStateFlow()

    /**
     * Refreshes the state of the Blocks World by retrieving the current state from the world instance.
     */
    suspend fun refresh() {
        _state.value = world.getState()
    }
}

/**
 * State holder for the Blocks World application, managing the goal, world state, and agent execution.
 */
class BlocksWorldAppState {

    /**
     * The seed value used for initializing the Blocks World random generator.
     */
    var seed by mutableStateOf("42")

    /**
     * The number of blocks in the Blocks World.
     */
    var blockCount by mutableStateOf("10")

    /**
     *  The textual representation of the goal for the Blocks World application, which can be parsed into a PrologGoal.
     */
    var goalText by mutableStateOf("[A, B]; [C, D, E]; [F]")
        private set

    /**
     * Sets the goal for the Blocks World application by parsing the provided text into a PrologGoal.
     */
    fun setGoal(text: String) {
        goalText = text
        goal = parseGoal(text)
    }

    /**
     * The current goal for the Blocks World application, represented as a PrologGoal.
     * It is initialized with a default goal representing the desired state of the blocks.
     */
    var goal by mutableStateOf(
        initialGoal {
            "state"(
                logicListOf(
                    logicListOf(Atom.of("A"), Atom.of("B")),
                    logicListOf(Atom.of("C"), Atom.of("D"), Atom.of("E")),
                    logicListOf(Atom.of("F")),
                ),
            )
        },
    )

    /**
     * The current instance of the BlocksWorld, initialized with a default seed and block count.
     */
    var world by mutableStateOf(
        BlocksWorld(42, 10),
    )
        private set

    /**
     * The ViewModel for the Blocks World application, which manages the state
     * and interactions with the BlocksWorld instance.
     */
    var vm by mutableStateOf(
        BlocksWorldViewModel(world),
    )
        private set

    private var agentJob: Job? = null

    /**
     * Resets the Blocks World application state by stopping any running agent.
     */
    fun reset() {
        stop()

        val newSeed = seed.toLongOrNull() ?: 42L
        val newBlockCount = blockCount.toIntOrNull() ?: 10

        world = BlocksWorld(newSeed, newBlockCount)
        vm = BlocksWorldViewModel(world)
    }

    /**
     * Starts the agent to play the Blocks World game using the current goal and world state.
     */
    fun play(scope: CoroutineScope) {
        if (agentJob?.isActive == true) {
            return
        }

        val currentGoal = goal

        agentJob = scope.launch {
            mas(LocalNodeBuilder()) {
                blocksWorldNode(world, currentGoal)
            }.run(CoroutineNodeRunner())
        }
    }

    /**
     * Stops the agent if it is currently running, cancelling its job and setting it to null.
     */
    fun stop() {
        agentJob?.cancel()
        agentJob = null
    }

    /**
     * Parses the provided text representation of the goal into a PrologGoal.
     */
    fun parseGoal(text: String): PrologGoal {
        val stacks = text
            .split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { stackText ->

                val cleaned = stackText
                    .removePrefix("[")
                    .removeSuffix("]")

                val blocks = cleaned
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { Atom.of(it) }

                List.of(*blocks.toTypedArray())
            }

        return initialGoal {
            "state"(logicListOf(*stacks.toTypedArray()))
        }
    }
}
