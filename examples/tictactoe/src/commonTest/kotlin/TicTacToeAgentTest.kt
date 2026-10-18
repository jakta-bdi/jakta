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
     * Builds a square board from rows like "xx.", where '.' is an empty cell.
     */
    private fun board(vararg rows: String) = BoardState(
        size = rows.size,
        cells = rows.flatMap { row -> row.map { Mark.entries.firstOrNull { m -> m.symbol == it.toString() } } },
    )

    /**
     * Lets the X agent move once from [start] (O is left to a user who never plays) and returns the board after it.
     */
    private fun firstMoveOfX(start: BoardState, check: (BoardState) -> Unit) = runTest {
        val board = Board(start)
        val game = launch {
            mas(NodeBuilders.baseNode()) {
                ticTacToeNode(board, mapOf(X to Player.AGENT, O to Player.HUMAN), thinkTime = {
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

    /**
     * The empty cells where [mark] would complete a line.
     */
    private fun BoardState.winningCells(mark: Mark) = cells.indices.filter { i ->
        cells[i] == null && copy(cells = cells.toMutableList().also { it[i] = mark }).winner == mark
    }

    @Test
    fun largerBoardsAreWonWithOneMarkLessThanTheirSize() {
        assertEquals(X, board("xxx.", "oo..", "....", "o...").winner)
        assertEquals(X, board("xxxx.", "oo...", ".....", "o....", "o....").winner)
        assertEquals(null, board("xx.", "oo.", "...").winner)
    }

    @Test
    fun agentForksOnALargerBoard() = firstMoveOfX(board("....", ".x..", "....", "...o")) {
        assertTrue(it.winningCells(X).size >= 2, "X should threaten two lines after its move")
    }

    @Test
    fun agentBlocksAForkOnALargerBoard() = firstMoveOfX(board("...x", "o..o", "....", "...x")) {
        assertEquals(X, it[0, 2])
    }

    @Test
    fun agentAnswersTheUsersMove() = runTest {
        val board = Board(BoardState(3))
        lateinit var game: TicTacToeEnvironment
        val system = mas(NodeBuilders.baseNode()) {
            game = ticTacToeNode(board, mapOf(X to Player.HUMAN, O to Player.AGENT), thinkTime = { Duration.ZERO })
        }
        val run = launch { system.run(CoroutineNodeRunner(SharedMemoryNetwork())) }
        game.put(1, 1, X)
        val answered = board.state.first { state -> state.cells.count { it == O } == 1 }
        run.cancel()
        assertEquals(X, answered[1, 1])
    }

    @Test
    fun twoAgentsPlayUntilTheGameIsOver() = runTest {
        val board = Board(BoardState(3))
        mas(NodeBuilders.baseNode()) {
            ticTacToeNode(board, mapOf(X to Player.AGENT, O to Player.AGENT), thinkTime = {
                Duration.ZERO
            })
        }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
        assertTrue(board.state.value.isOver)
    }
}
