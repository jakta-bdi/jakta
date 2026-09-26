import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import model.Board
import model.BoardState
import model.Mark
import model.Mark.O
import model.Mark.X

class TicTacToeAgentTest {

    /**
     * Builds a 3×3 board from rows like "xx.", where '.' is an empty cell.
     */
    private fun board(vararg rows: String) = BoardState(
        size = 3,
        cells = rows.flatMap { row -> row.map { Mark.entries.firstOrNull { m -> m.symbol == it.toString() } } },
    )

    /**
     * Lets the X agent move once from [start] (O is a human who never clicks) and returns the board after it.
     */
    private fun firstMoveOfX(start: BoardState, check: (BoardState) -> Unit) = runTest {
        val board = Board(start)
        val game = launch {
            mas(NodeBuilders.baseNode()) {
                ticTacToeNode(board, mapOf(X to Player.AGENT, O to Player.HUMAN), HumanMoves(), thinkTime = {
                    Duration.ZERO
                })
            }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        }
        check(board.state.first { it != start })
        game.cancel()
    }

    @Test
    fun agentCompletesItsLineRatherThanBlocking() = firstMoveOfX(board("xx.", "oo.", "...")) {
        assertEquals(X, it[2, 0])
        assertEquals(X, it.winner)
    }

    @Test
    fun agentBlocksTheOpponentsLine() = firstMoveOfX(board("x..", "oo.", "..x")) {
        assertEquals(X, it[2, 1])
    }

    @Test
    fun twoAgentsPlayUntilTheGameIsOver() = runTest {
        val board = Board(BoardState(3))
        mas(NodeBuilders.baseNode()) {
            ticTacToeNode(board, mapOf(X to Player.AGENT, O to Player.AGENT), HumanMoves(), thinkTime = {
                Duration.ZERO
            })
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertTrue(board.state.value.isOver)
    }
}
