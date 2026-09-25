import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.initialGoal
import it.unibo.jakta.dsl.goal.matchingGoal
import it.unibo.jakta.dsl.mas.MasBuilder
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.plan.satisfies
import it.unibo.jakta.dsl.plan.triggers
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.JaktaLogicProgrammingScope.Companion.prologPlan
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.print
import it.unibo.jakta.value
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import kotlin.random.Random
import kotlin.time.Duration
import model.Board
import model.Mark

/**
 * Directions in which a line can be aligned, as steps along columns and rows.
 */
private val directions = mapOf(
    "horizontal" to (1 to 0),
    "vertical" to (0 to 1),
    "diagonal" to (1 to 1),
    "antidiagonal" to (1 to -1),
)

/**
 * The board size on which the agent's strategy is optimal: it never loses.
 */
private const val OPTIMAL_SIZE = 3

private val join = Atom.of("join")
private val empty = Atom.of("e")

private fun cell(x: Term, y: Term, mark: Term): Struct = Struct.of("cell", x, y, mark)
private fun turn(mark: Mark): Struct = Struct.of("turn", Atom.of(mark.symbol))

/**
 * A line of [size] cells holding [owner]'s mark except for the empty cell `cell(X, Y, e)` at [gap].
 */
private fun JaktaLogicProgrammingScope.lineWithGap(size: Int, gap: Int, owner: Term): Struct = "aligned"(
    logicListOf((0 until size).map { if (it == gap) cell(X, Y, empty) else cell(`_`, `_`, owner) }),
)

/**
 * A line of [size] cells holding mark `M` except for two empty cells, `cell(X, Y, e)` at [move]
 * and `cell(A, B, e)` at [rest]: playing (X, Y) would leave (A, B) to complete the line.
 */
private fun JaktaLogicProgrammingScope.lineWithTwoGaps(size: Int, move: Int, rest: Int): Struct = "aligned"(
    logicListOf(
        (0 until size).map {
            when (it) {
                move -> cell(X, Y, empty)
                rest -> cell(A, B, empty)
                else -> cell(`_`, `_`, M)
            }
        },
    ),
)

private fun eq(left: Term, right: Term): Struct = Struct.of("=", left, right)

/**
 * Creates the node of the game, with one player for each mark.
 */
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.ticTacToeNode(
    board: Board,
    players: Map<Mark, Player>,
    humanMoves: HumanMoves,
    thinkTime: () -> Duration,
    mistakeChance: () -> Double = { 0.0 },
    random: Random = Random.Default,
) = node {
    val game = TicTacToeEnvironment(board, node, humanMoves, thinkTime, mistakeChance, random)
    for ((mark, player) in players) {
        val name = if (player == Player.HUMAN) "human-${mark.symbol}" else "${mark.symbol}-agent"
        agent<PrologBelief, PrologGoal>(BaseAgentID(name)) {
            when (player) {
                Player.AGENT -> agentPlayer(game, mark, board.state.value.size)
                Player.HUMAN -> humanPlayer(game, mark)
            }
        }
    }
}

/**
 * What every player does: perceive the board and, if moving first, start the game.
 */
private fun AgentBuilder<PrologBelief, PrologGoal, Any>.commonBehaviour(mark: Mark) {
    embodiedAs { Any() }
    handlesPerceptionEvents {
        when (it) {
            is BoardPerception -> handleBoardPerception(it, beliefs)
            else -> null
        }
    }
    if (mark == Mark.X) {
        hasInitialGoals { !initialGoal { join } }
    }
}

/**
 * A player that waits for the human to click a cell whenever it is its turn.
 */
private fun AgentBuilder<PrologBelief, PrologGoal, Any>.humanPlayer(game: TicTacToeSkills, mark: Mark) {
    commonBehaviour(mark)
    hasPlanLibrary {
        prologPlan {
            adding.goal { matchingGoal { join } } triggers { game.join() }
        }
        prologPlan {
            adding.belief { matchingBelief { turn(mark) } } triggers {
                val (x, y) = game.humanMove()
                game.put(x, y, mark)
            }
        }
    }
}

/**
 * A BDI player following the classic strategy for tic-tac-toe (Newell and Simon), which never loses on 3×3;
 * on larger boards it only completes lines, blocks them and takes good cells.
 * On its turn, the first applicable plan moves. Lines, threats and forks are recognised by rules, and both
 * rules and plans are generated in Kotlin for the board [size]. When the environment says it is `distracted`,
 * it plays a random cell instead.
 */
private fun AgentBuilder<PrologBelief, PrologGoal, Any>.agentPlayer(game: TicTacToeSkills, mark: Mark, size: Int) {
    val me = Atom.of(mark.symbol)
    val opponent = Atom.of(mark.other.symbol)
    val last = size - 1
    val corners = listOf(0 to 0, last to 0, 0 to last, last to last)
    commonBehaviour(mark)
    believes {
        for ((direction, step) in directions) {
            val (dx, dy) = step
            // a line of one cell, or a cell followed by the rest of the line, one step further
            +inferenceRule { direction(logicListOf(cell(X, Y, S))) impliedBy cell(X, Y, S) }
            +inferenceRule {
                direction(logicList(cell(A, B, C), cell(X, Y, S), tail = T)) impliedBy (
                    cell(A, B, C) and
                        (X `is` (A + dx)) and
                        (Y `is` (B + dy)) and
                        cell(X, Y, S) and
                        direction(logicList(cell(X, Y, S), tail = T))
                    )
            }
            +inferenceRule { "aligned"(L) impliedBy direction(L) }
        }
        // the fork strategy is optimal on 3×3; on larger boards it is not, and too slow to be worth it
        if (size == OPTIMAL_SIZE) {
            // threat(X, Y, M, A, B): if M plays (X, Y), it threatens to complete a line at (A, B)
            for (move in 0 until size) {
                for (rest in (0 until size) - move) {
                    +inferenceRule { "threat"(X, Y, M, A, B) impliedBy lineWithTwoGaps(size, move, rest) }
                }
            }
            // fork(X, Y, M): if M plays (X, Y), it threatens two lines at once, and cannot be stopped
            +inferenceRule {
                "fork"(X, Y, M) impliedBy (
                    "threat"(X, Y, M, A, B) and "threat"(X, Y, M, C, D) and not(eq(A, C) and eq(B, D))
                    )
            }
        }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal { matchingGoal { join } } triggers { game.join() }
        }

        /** A plan for our turn that plays the cell (X, Y) found by [guard], explaining why. */
        fun move(why: String, guard: JaktaLogicProgrammingScope.() -> Struct) = prologPlan {
            adding.belief { matchingBelief { turn(mark) } } onlyWhen {
                satisfies(guard)
            } triggers {
                agent.print(why, ": playing (", X, ", ", Y, ")")
                game.think()
                game.put(X.value(), Y.value(), mark)
            }
        }

        move("Oops, I got distracted") { "distracted"(me) and cell(X, Y, empty) }
        for (gap in 0 until size) move("I can complete a line and win") { lineWithGap(size, gap, me) }
        for (gap in 0 until size) {
            move("The opponent could complete a line, blocking") {
                lineWithGap(size, gap, opponent)
            }
        }
        if (size == OPTIMAL_SIZE) {
            move("Creating a fork") { "fork"(X, Y, me) }
            move("The opponent could fork only here, taking it") {
                "fork"(X, Y, opponent) and not("fork"(A, B, opponent) and not(eq(A, X) and eq(B, Y)))
            }
            move("The opponent could fork in many places: threatening, so it must answer where it cannot fork") {
                "fork"(`_`, `_`, opponent) and "threat"(X, Y, me, A, B) and not("fork"(A, B, opponent))
            }
            move("Blocking a fork") { "fork"(X, Y, opponent) }
        }
        if (size % 2 == 1) {
            val centre = Integer.of(size / 2)
            move("Taking the centre") { eq(X, centre) and eq(Y, centre) and cell(X, Y, empty) }
        }
        for ((x, y) in corners) {
            move("Taking the corner opposite to the opponent") {
                eq(X, Integer.of(x)) and eq(Y, Integer.of(y)) and cell(X, Y, empty) and
                    cell(Integer.of(last - x), Integer.of(last - y), opponent)
            }
        }
        for ((x, y) in corners) {
            move("Taking a corner") { eq(X, Integer.of(x)) and eq(Y, Integer.of(y)) and cell(X, Y, empty) }
        }
        move("Nothing better to do") { cell(X, Y, empty) }
    }
}
