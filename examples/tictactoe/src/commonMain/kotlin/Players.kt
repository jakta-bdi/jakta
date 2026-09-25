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

private val join = Atom.of("join")
private val empty = Atom.of("e")

private fun cell(x: Term, y: Term, mark: Term): Struct = Struct.of("cell", x, y, mark)
private fun turn(mark: Mark): Struct = Struct.of("turn", Atom.of(mark.symbol))

/**
 * A line of [size] cells holding [owner]'s mark except for the empty cell `cell(X, Y, e)` at [gap].
 */
private fun JaktaLogicProgrammingScope.lineWithGap(size: Int, gap: Int, owner: Mark): Struct = "aligned"(
    logicListOf((0 until size).map { if (it == gap) cell(X, Y, empty) else cell(`_`, `_`, Atom.of(owner.symbol)) }),
)

/**
 * Creates the node of the game, with one player for each mark.
 */
fun MasBuilder<BaseNode<Any>, BaseNodeBuilder<Any, BaseNode<Any>>>.ticTacToeNode(
    board: Board,
    players: Map<Mark, Player>,
    humanMoves: HumanMoves,
    thinkTime: () -> Duration,
) = node {
    val game = TicTacToeEnvironment(board, node, humanMoves, thinkTime)
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
 * A BDI player. On its turn, the first applicable plan wins: complete a line, block the opponent's line,
 * take the centre, or play anywhere. Lines are recognised by rules, and both rules and plans are generated
 * for the board [size].
 */
private fun AgentBuilder<PrologBelief, PrologGoal, Any>.agentPlayer(game: TicTacToeSkills, mark: Mark, size: Int) {
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
    }
    hasPlanLibrary {
        prologPlan {
            adding.goal { matchingGoal { join } } triggers { game.join() }
        }
        for (gap in 0 until size) {
            prologPlan {
                adding.belief { matchingBelief { turn(mark) } } onlyWhen {
                    satisfies { lineWithGap(size, gap, mark) }
                } triggers {
                    agent.print("I can complete a line at (", X, ", ", Y, "): I win!")
                    game.think()
                    game.put(X.value(), Y.value(), mark)
                }
            }
        }
        for (gap in 0 until size) {
            prologPlan {
                adding.belief { matchingBelief { turn(mark) } } onlyWhen {
                    satisfies { lineWithGap(size, gap, mark.other) }
                } triggers {
                    agent.print("The opponent could complete a line: blocking (", X, ", ", Y, ")")
                    game.think()
                    game.put(X.value(), Y.value(), mark)
                }
            }
        }
        if (size % 2 == 1) {
            val centre = Integer.of(size / 2)
            prologPlan {
                adding.belief { matchingBelief { turn(mark) } } onlyWhen {
                    satisfies { cell(centre, centre, empty) }
                } triggers {
                    agent.print("Taking the centre")
                    game.think()
                    game.put(size / 2, size / 2, mark)
                }
            }
        }
        prologPlan {
            adding.belief { matchingBelief { turn(mark) } } onlyWhen {
                satisfies { cell(X, Y, empty) }
            } triggers {
                agent.print("Nothing better to do: playing (", X, ", ", Y, ")")
                game.think()
                game.put(X.value(), Y.value(), mark)
            }
        }
    }
}
