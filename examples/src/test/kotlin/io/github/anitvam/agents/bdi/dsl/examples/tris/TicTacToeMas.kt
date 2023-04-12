@file:Suppress("UNUSED_PARAMETER")

package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.Jakta
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.fromPercept
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.allDistinctPermutationsOf
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.cell
import io.github.anitvam.agents.bdi.dsl.mas
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.operators.OperatorSet

fun BeliefsScope.alignment(name: String, dx: Int, dy: Int) {
    rule { name(listOf(cell(X, Y, Z))) impliedBy (cell(X, Y, Z).fromPercept) }
    rule {
        val second = cell(X, Y, Z)
        name(listFrom(cell(A, B, C), second, last = W)) .impliedBy(
            cell(A, B, C).fromPercept,
            second.fromPercept,
            (X - A) arithEq dx,
            (Y - B) arithEq dy,
            name(listFrom(second, last = W))
        )
    }
}

fun ticTacToe(n: Int = 3) = mas {
    environment {
        from(GridEnvironment(n))
        actions {
            action(Put)
        }
    }
    agent("x-agent") {
        beliefs {
            alignment("vertical", dx = 0, dy = 1)
            alignment("horizontal", dx = 1, dy = 0)
            alignment("diagonal", dx = 1, dy = 1)
            alignment("antidiagonal", dx = 1, dy = -1)
            for (direction in arrayOf("vertical", "horizontal", "diagonal", "antidiagonal")) {
                rule { "aligned"(L) impliedBy direction(L)  }
            }
        }
        goals { achieve("turn"("self")) }
        plans {
            + achieve("turn"("self")) onlyIf {
                "aligned"((1..n).map { cell(symbol = "x") })
            } then {
                iact("print"("I won!"))
            }
            + achieve("turn"("self")) onlyIf {
                "aligned"((1..n).map { cell(symbol = "o") })
            } then {
                iact("print"("I lost!"))
            }
            for (winningLine in allDistinctPermutationsOf(cell(X, Y, "e"), cell(symbol = "x"), n - 1)) {
                + achieve("turn"("self")) onlyIf {"aligned"(winningLine) } then {
                    act("put"(X, Y, "x"))
                }
            }
            for (winningLine in allDistinctPermutationsOf(cell(X, Y, "e"), cell(symbol = "x"), cell(symbol = "e") )) {
                + achieve("turn"("self")) onlyIf {"aligned"(winningLine) } then {
                    act("put"(X, Y, "x"))
                }
            }
            + achieve("turn"("self")) onlyIf {
                cell(X, Y, "e").fromPercept
            } then {
                act("put"(X, Y, "x"))
            }
        }
    }
}

fun main() {
    val system = ticTacToe(3)
    for (agent in system.agents) {
        println("% ${agent.name}")
        for (plan in agent.context.planLibrary.plans) {
            val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
            println("+!${formatter.format(plan.trigger.value)} " +
                    ": ${formatter.format(plan.guard)} " +
                    "<- ${plan.goals.joinToString("; "){ formatter.format(it.value) }}")
        }
    }
}
