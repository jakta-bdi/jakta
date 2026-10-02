import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.Board
import model.BoardState
import model.Mark

/**
 * Plays the agent against every possible opponent on 3×3: it must never lose. JVM only, as it runs many games.
 */
class TicTacToeOptimalityTest {

    /**
     * Seeds for the order in which cells are perceived, i.e. for choosing between equally good moves.
     */
    private val seeds = 0 until 2

    private suspend fun agentMove(start: BoardState, seed: Int): BoardState = coroutineScope {
        val board = Board(start)
        val agent = start.turn
        val game = launch {
            mas(NodeBuilders.baseNode()) {
                ticTacToeNode(
                    board,
                    mapOf(agent to Player.AGENT, agent.other to Player.HUMAN),
                    HumanMoves(),
                    thinkTime = { Duration.ZERO },
                    random = Random(seed),
                )
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }
        board.state.first { it != start }.also { game.cancel() }
    }

    private fun BoardState.render() = (0 until size).joinToString("/") { y ->
        (0 until size).joinToString("") { x -> get(x, y)?.symbol ?: "." }
    }

    private fun neverLoses(agent: Mark) = runTest(timeout = 10.minutes) {
        val visited = mutableSetOf<BoardState>()

        suspend fun explore(state: BoardState) {
            if (!visited.add(state)) return
            if (state.winner == agent.other) fail("The agent ($agent) lost: ${state.render()}")
            if (state.isOver) return
            val next = if (state.turn == agent) {
                seeds.map { agentMove(state, it) }.toSet()
            } else {
                state.cells.indices.filter { state.cells[it] == null }.map { i ->
                    state.copy(cells = state.cells.toMutableList().also { it[i] = agent.other })
                }
            }
            next.forEach { explore(it) }
        }

        explore(BoardState(3))
        println("The agent ($agent) never lost, in ${visited.size} positions")
    }

    /** The agent moves first. */
    @Test
    fun agentNeverLosesAsX() = neverLoses(Mark.X)

    /** The opponent moves first. */
    @Test
    fun agentNeverLosesAsO() = neverLoses(Mark.O)
}
