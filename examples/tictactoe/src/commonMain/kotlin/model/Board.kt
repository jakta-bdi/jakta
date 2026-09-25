package model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The two marks players put on the board; X always moves first.
 */
enum class Mark {
    /** The mark of the first player. */
    X,

    /** The mark of the second player. */
    O,
    ;

    /**
     * The mark of the opponent.
     */
    val other: Mark get() = if (this == X) O else X

    /**
     * The atom that stands for this mark in the agents' beliefs.
     */
    val symbol: String get() = name.lowercase()
}

/**
 * A snapshot of an n×n board; cells are indexed by column [x] and row [y].
 *
 * @property size the number of rows and columns.
 * @property cells the mark in each cell, row by row, or null if the cell is empty.
 */
data class BoardState(val size: Int, val cells: List<Mark?> = List(size * size) { null }) {

    /**
     * The mark at column [x] and row [y], or null if the cell is empty.
     */
    operator fun get(x: Int, y: Int): Mark? = cells[y * size + x]

    /**
     * Every row, column and diagonal, as lists of (x, y) coordinates.
     */
    val lines: List<List<Pair<Int, Int>>> by lazy {
        val indices = 0 until size
        indices.map { y -> indices.map { x -> x to y } } +
            indices.map { x -> indices.map { y -> x to y } } +
            listOf(indices.map { it to it }, indices.map { it to size - 1 - it })
    }

    /**
     * The line completed by a single mark, if any.
     */
    val winningLine: List<Pair<Int, Int>>? get() = lines.firstOrNull { line ->
        val first = get(line[0].first, line[0].second)
        first != null && line.all { (x, y) -> get(x, y) == first }
    }

    /**
     * The mark that completed a line, if any.
     */
    val winner: Mark? get() = winningLine?.first()?.let { (x, y) -> get(x, y) }

    /**
     * Whether nobody can move any more.
     */
    val isOver: Boolean get() = winner != null || cells.none { it == null }

    /**
     * Whose turn it is: X moves first, so it is X's turn whenever both have moved equally often.
     */
    val turn: Mark get() = if (cells.count { it == Mark.X } == cells.count { it == Mark.O }) Mark.X else Mark.O
}

/**
 * The board shared by the players.
 *
 * @param initial the initial state, usually an empty board.
 */
class Board(initial: BoardState) {
    private val mutableState = MutableStateFlow(initial)

    /**
     * The current state of the board.
     */
    val state: StateFlow<BoardState> = mutableState.asStateFlow()

    /**
     * Puts [mark] at column [x] and row [y], which must be empty, when it is [mark]'s turn.
     */
    fun put(x: Int, y: Int, mark: Mark) {
        mutableState.update { board ->
            require(!board.isOver) { "The game is over" }
            require(board.turn == mark) { "It is not $mark's turn" }
            require(board[x, y] == null) { "Cell ($x, $y) is not empty" }
            board.copy(cells = board.cells.toMutableList().also { it[y * board.size + x] = mark })
        }
    }
}
