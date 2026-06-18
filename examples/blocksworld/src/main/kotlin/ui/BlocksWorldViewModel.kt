package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import blockA
import blockB
import blockC
import blockD
import blockE
import blockF
import blockG
import blockH
import blockI
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

class BlocksWorldViewModel(private val world: BlocksWorld) {

    private val _state = MutableStateFlow(runBlocking{ world.getState()})
    val state = _state.asStateFlow()

    suspend fun refresh() {
        _state.value = world.getState()
    }
}


class BlocksWorldAppState {

    var seed by mutableStateOf("42")
    var blockCount by mutableStateOf("10")

    var goalText by mutableStateOf("[A, B]; [C, D, E]; [F]")
        private set

    fun setGoal(text: String) {
        goalText = text
        goal = parseGoal(text)
    }

    var goal by mutableStateOf( initialGoal {
        "state"(
            logicListOf(
                logicListOf(Atom.of("A"), Atom.of("B")),
                logicListOf(Atom.of("C"), Atom.of("D"), Atom.of("E")),
                logicListOf(Atom.of("F"))
            )
        )
    })

    var world by mutableStateOf(
        BlocksWorld(42, 10)
    )
        private set

    var vm by mutableStateOf(
        BlocksWorldViewModel(world)
    )
        private set

    private var agentJob: Job? = null

    fun reset() {
        stop()

        val newSeed = seed.toLongOrNull() ?: 42L
        val newBlockCount = blockCount.toIntOrNull() ?: 10

        world = BlocksWorld(newSeed, newBlockCount)
        vm = BlocksWorldViewModel(world)
    }

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

    fun stop() {
        agentJob?.cancel()
        agentJob = null
    }

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
