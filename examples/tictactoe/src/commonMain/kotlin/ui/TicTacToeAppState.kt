package ui

import HumanMoves
import Player
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import it.unibo.jakta.dsl.mas
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import model.Board
import model.BoardState
import model.Mark
import ticTacToeNode

private const val DEFAULT_THINK_TIME_MS = 600

/**
 * The board sizes the user can choose.
 */
val BOARD_SIZES = 3..5

/**
 * How often agents get distracted and play a random cell instead of their best move.
 *
 * @property mistakeChance the probability of a random move on each turn.
 */
@Suppress("MagicNumber")
enum class Difficulty(val mistakeChance: Double) {
    /** Makes many mistakes. */
    EASY(0.5),

    /** Makes a mistake now and then. */
    MEDIUM(0.2),

    /** Never makes mistakes: on 3×3, the best you can get is a draw. */
    UNBEATABLE(0.0),
}

/**
 * State holder for the Tic-Tac-Toe application: who plays, the board, and the running game.
 *
 * @property agentDispatcher where the agents run, off the UI thread on desktop.
 */
class TicTacToeAppState(private val agentDispatcher: CoroutineDispatcher = Dispatchers.Default) {

    init {
        AgentTrace.install()
    }

    /**
     * Who plays each mark.
     */
    var players by mutableStateOf(mapOf(Mark.X to Player.AGENT, Mark.O to Player.HUMAN))
        private set

    /**
     * The number of rows and columns of the next game.
     */
    var size by mutableStateOf(BOARD_SIZES.first)
        private set

    /**
     * The board of the current (or last) game.
     */
    var board by mutableStateOf(Board(BoardState(size)))
        private set

    /**
     * How often agents make mistakes; it can be changed during a game.
     */
    var difficulty by mutableStateOf(Difficulty.MEDIUM)

    /**
     * How long agents think before each move.
     */
    var thinkTime: Duration by mutableStateOf(DEFAULT_THINK_TIME_MS.milliseconds)

    /**
     * Whether a game is being played.
     */
    var isRunning by mutableStateOf(false)
        private set

    private var humanMoves = HumanMoves()
    private var gameJob: Job? = null

    /**
     * Changes who plays [mark]; takes effect from the next game.
     */
    fun togglePlayer(mark: Mark) {
        val next = if (players.getValue(mark) == Player.AGENT) Player.HUMAN else Player.AGENT
        players = players + (mark to next)
    }

    /**
     * Changes the board size; takes effect from the next game.
     */
    fun changeSize(newSize: Int) {
        size = newSize.coerceIn(BOARD_SIZES)
        if (!isRunning) board = Board(BoardState(size))
    }

    /**
     * Forwards a click on a cell to the human player, if it is their turn.
     */
    fun click(x: Int, y: Int) {
        val state = board.state.value
        if (isRunning && players[state.turn] == Player.HUMAN && state[x, y] == null) humanMoves.click(x, y)
    }

    /**
     * Abandons any game in progress and starts a new one; the referee ends it when it is over.
     */
    fun newGame(scope: CoroutineScope) {
        gameJob?.cancel()
        AgentTrace.clear()
        val currentBoard = Board(BoardState(size)).also { board = it }
        val currentPlayers = players
        val currentMoves = HumanMoves().also { humanMoves = it }
        isRunning = true
        val job = scope.launch(agentDispatcher, start = CoroutineStart.LAZY) {
            try {
                mas(NodeBuilders.baseNode()) {
                    ticTacToeNode(
                        currentBoard,
                        currentPlayers,
                        currentMoves,
                        thinkTime = { thinkTime },
                        mistakeChance = { difficulty.mistakeChance },
                    )
                }.run(CoroutineNodeRunner(SharedMemoryNetwork()))
            } finally {
                // an abandoned game must not flag a newer one as finished
                if (gameJob === coroutineContext.job) isRunning = false
            }
        }
        gameJob = job
        job.start()
    }
}
