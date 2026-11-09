@file:Suppress("StringLiteralDuplication", "LongMethod") // example code: DSL definitions read best in one place

import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.achieve
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.dsl.belief.PrologBelief
import it.unibo.jakta.dsl.belief.inferenceRule
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.dsl.goal.goal
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
import it.unibo.tuprolog.core.Var
import kotlin.random.Random
import kotlin.time.Duration
import model.Board
import model.BoardState
import model.Mark

private val join = Atom.of("join")
private val moveGoal = Atom.of("move")
private val empty = Atom.of("e")

private fun cell(x: Term, y: Term, mark: Term): Struct = Struct.of("cell", x, y, mark)
private fun turn(mark: Mark): Struct = Struct.of("turn", Atom.of(mark.symbol))

/**
 * A line of [length] cells holding [owner]'s mark except for the empty cell `cell(X, Y, e)` at [gap].
 */
private fun JaktaLogicProgrammingScope.lineWithGap(length: Int, gap: Int, owner: Term): Struct = "aligned"(
    logicListOf((0 until length).map { if (it == gap) cell(X, Y, empty) else cell(`_`, `_`, owner) }),
)

/**
 * A line of [length] cells holding mark `M` except for two empty cells, `cell(X, Y, e)` at [move]
 * and `cell(A, B, e)` at [rest]: playing (X, Y) would leave (A, B) to complete the line.
 */
private fun JaktaLogicProgrammingScope.lineWithTwoGaps(length: Int, move: Int, rest: Int): Struct = "aligned"(
    logicListOf(
        (0 until length).map {
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
 * Creates the node of the game, with an agent for each mark played by [Player.AGENT];
 * the user plays the other marks through the returned environment.
 */
@Suppress("LongParameterList") // game settings with defaults, kept flat for the callers
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.ticTacToeNode(
    board: Board,
    players: Map<Mark, Player>,
    thinkTime: () -> Duration,
    mistakeChance: () -> Double = { 0.0 },
    random: Random = Random.Default,
): TicTacToeEnvironment {
    lateinit var game: TicTacToeEnvironment
    node {
        game = TicTacToeEnvironment(board, node, thinkTime, mistakeChance, random)
        for ((mark, player) in players) {
            if (player == Player.AGENT) {
                agent<PrologBelief, PrologGoal>(BaseAgentID("${mark.symbol}-agent")) {
                    agentPlayer(game, mark, board.state.value)
                }
            }
        }
    }
    return game
}

/**
 * What every agent does: perceive the board, joining the game to see it a first time.
 */
private fun AgentBuilder<PrologBelief, PrologGoal, Any>.commonBehaviour() {
    embodiedAs { Any() }
    handlesPerceptionEvents {
        when (it) {
            is BoardPerception -> handleBoardPerception(it, beliefs)
            else -> null
        }
    }
    hasInitialGoals { !initialGoal { join } }
}

/**
 * A BDI player following the classic strategy for tic-tac-toe (Newell and Simon), which never loses on 3×3;
 * on larger boards the same strategy (lines, forks, good cells) is a strong opponent, though not an optimal one.
 * On its turn, it thinks and then pursues the goal `move`, achieved by the first applicable plan.
 * Lines, threats and forks are recognised by rules, and both rules and plans are generated in Kotlin
 * for the size and winning length of the [board]. When the environment says it is `distracted`,
 * it plays a random cell instead.
 */
private fun AgentBuilder<PrologBelief, PrologGoal, Any>.agentPlayer(
    game: TicTacToeSkills,
    mark: Mark,
    board: BoardState,
) {
    val size = board.size
    val length = board.winLength
    val me = Atom.of(mark.symbol)
    val opponent = Atom.of(mark.other.symbol)
    val last = size - 1
    val corners = listOf(0 to 0, last to 0, 0 to last, last to last)
    commonBehaviour()
    believes {
        // aligned([cell(X1, Y1, S1), ...]): one rule for each line of the board, as long as needed to win
        for (line in board.lines) {
            val cells = line.mapIndexed { i, (x, y) -> cell(Integer.of(x), Integer.of(y), Var.of("S$i")) }
            +inferenceRule { "aligned"(logicListOf(cells)) impliedBy cells.reduce<Struct, Struct> { a, b -> a and b } }
        }
        // threat(X, Y, M, A, B): if M plays (X, Y), it threatens to complete a line at (A, B)
        for (move in 0 until length) {
            for (rest in (0 until length) - move) {
                +inferenceRule { "threat"(X, Y, M, A, B) impliedBy lineWithTwoGaps(length, move, rest) }
            }
        }
        // fork(X, Y, M): if M plays (X, Y), it threatens two lines at once, and cannot be stopped
        +inferenceRule {
            "fork"(X, Y, M) impliedBy (
                "threat"(X, Y, M, A, B) and "threat"(X, Y, M, C, D) and not(eq(A, C) and eq(B, D))
                )
        }
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal { matchingGoal { join } } triggers { game.join() }
        }
        // think before choosing the move, which also lets the UI show the opponent's last move meanwhile
        prologPlan {
            adding.belief { matchingBelief { turn(mark) } } triggers {
                game.think()
                agent.achieve(goal { moveGoal })
            }
        }

        /** A plan to move that plays the cell (X, Y) found by [guard], explaining why. */
        fun move(why: String, guard: JaktaLogicProgrammingScope.() -> Struct) = prologPlan {
            adding.goal { matchingGoal { moveGoal } } onlyWhen {
                satisfies(guard)
            } triggers {
                agent.print(why, ": playing (", X, ", ", Y, ")")
                game.put(X.value(), Y.value(), mark)
            }
        }

        move("Oops, I got distracted") { "distracted"(me) and cell(X, Y, empty) }
        for (gap in 0 until length) move("I can complete a line and win") { lineWithGap(length, gap, me) }
        for (gap in 0 until length) {
            move("The opponent could complete a line, blocking") {
                lineWithGap(length, gap, opponent)
            }
        }
        move("Creating a fork") { "fork"(X, Y, me) }
        move("The opponent could fork only here, taking it") {
            "fork"(X, Y, opponent) and not("fork"(A, B, opponent) and not(eq(A, X) and eq(B, Y)))
        }
        move("The opponent could fork in many places: threatening, so it must answer where it cannot fork") {
            "fork"(`_`, `_`, opponent) and "threat"(X, Y, me, A, B) and not("fork"(A, B, opponent))
        }
        move("Blocking a fork") { "fork"(X, Y, opponent) }
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
