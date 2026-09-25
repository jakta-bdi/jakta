import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.matchBelief
import it.unibo.jakta.dsl.belief.newContextBeliefQuery
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.node.Node
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import kotlin.random.Random
import kotlin.time.Duration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import model.Board
import model.BoardState
import model.Mark

/**
 * What players perceive: the whole board, and which agents got distracted this turn.
 *
 * @property board the current state of the board.
 * @property distracted the marks whose agent will make a random move instead of its best one.
 * @property random decides the order in which cells are perceived, i.e. which of equally good moves is chosen.
 */
data class BoardPerception(val board: BoardState, val distracted: Set<Mark>, val random: Random) : Perception

/**
 * Who plays each mark.
 */
enum class Player {
    /** A BDI agent, reasoning on the board. */
    AGENT,

    /** The user, clicking on the board. */
    HUMAN,
}

/**
 * The moves of the human player, as clicked in the UI.
 */
class HumanMoves {
    private val clicks = Channel<Pair<Int, Int>>(Channel.CONFLATED)

    /**
     * Records that the human clicked the cell at column [x] and row [y].
     */
    fun click(x: Int, y: Int) {
        clicks.trySend(x to y)
    }

    /**
     * Waits for the next click.
     */
    suspend fun next(): Pair<Int, Int> = clicks.receive()
}

/**
 * What players can do in the game.
 */
interface TicTacToeSkills {
    /**
     * Perceives the board, which starts the game.
     */
    suspend fun join()

    /**
     * Pauses for a while, so that humans can follow the game.
     */
    suspend fun think()

    /**
     * Puts [mark] at column [x] and row [y].
     */
    suspend fun put(x: Int, y: Int, mark: Mark)

    /**
     * Waits for the human to choose a cell.
     */
    suspend fun humanMove(): Pair<Int, Int>
}

/**
 * The game environment: it shows the board to every player after each move,
 * and acts as the referee that ends the game once somebody wins or the board is full.
 */
class TicTacToeEnvironment(
    private val board: Board,
    private val node: Node<*>,
    private val humanMoves: HumanMoves,
    private val thinkTime: () -> Duration,
    private val mistakeChance: () -> Double,
    private val random: Random,
) : TicTacToeSkills {

    private fun publish(state: BoardState) {
        val distracted = Mark.entries.filter { random.nextDouble() < mistakeChance() }.toSet()
        node.publishEvent(BoardPerception(state, distracted, random))
    }

    override suspend fun join() {
        publish(board.state.value)
    }

    override suspend fun think() {
        delay(thinkTime())
    }

    override suspend fun put(x: Int, y: Int, mark: Mark) {
        board.put(x, y, mark)
        val state = board.state.value
        publish(state)
        if (state.isOver) node.terminateNode()
    }

    override suspend fun humanMove(): Pair<Int, Int> = humanMoves.next()
}

private val cellQuery = newContextBeliefQuery { "cell"(X, Y, Z) }
private val turnQuery = newContextBeliefQuery { "turn"(X) }
private val distractedQuery = newContextBeliefQuery { "distracted"(X) }

/**
 * Updates `cell(X, Y, Mark)`, `turn(Mark)` and `distracted(Mark)` beliefs, where empty cells are marked `e`.
 * Only what changed is added or removed, so each move triggers only the new `turn` belief.
 */
fun handleBoardPerception(event: BoardPerception, beliefs: Collection<PrologBelief>): AgentUpdate<*> {
    val queries = listOf(cellQuery, turnQuery, distractedQuery)
    val old = beliefs.filter { belief -> queries.any { belief.matchBelief(it) != null } }.toSet()
    val distracted = event.distracted.map { Fact.of(Struct.of("distracted", Atom.of(it.symbol))) }
    val new = event.board.toBeliefs(event.random) + distracted
    return AgentUpdate.Belief(new - old, old - new)
}

private fun BoardState.toBeliefs(random: Random): Set<PrologBelief> {
    val indices = 0 until size
    // shuffled, so that agents looking for "any empty cell" do not always pick the same one
    val cells = indices.flatMap { x -> indices.map { y -> x to y } }.shuffled(random).map { (x, y) ->
        Fact.of(Struct.of("cell", Integer.of(x), Integer.of(y), Atom.of(get(x, y)?.symbol ?: "e")))
    }
    val turn = if (isOver) emptyList() else listOf(Fact.of(Struct.of("turn", Atom.of(turn.symbol))))
    return (cells + turn).toSet()
}
