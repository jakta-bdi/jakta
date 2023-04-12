package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.Jakta
import io.github.anitvam.agents.bdi.dsl.MasScope
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.fromPercept
import io.github.anitvam.agents.bdi.dsl.beliefs.selfSourced
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.allPossibleCombinationsOf
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
    generatePlayer("x", "o", n)
    generatePlayer("o", "x", n)
}

fun MasScope.generatePlayer(mySymbol: String, otherSymbol: String, gridDimension: Int) = agent("$mySymbol-agent") {
    beliefs {
        alignment("vertical", dx = 0, dy = 1)
        alignment("horizontal", dx = 1, dy = 0)
        alignment("diagonal", dx = 1, dy = 1)
        alignment("antidiagonal", dx = 1, dy = -1)
        for (direction in arrayOf("vertical", "horizontal", "diagonal", "antidiagonal")) {
            rule { "aligned"(L) impliedBy direction(L) }
        }
        fact { "other"("$otherSymbol-agent") }
    }
    goals { achieve("turn"("self")) }
    plans {
        arrayOf(mySymbol, otherSymbol).map { symbol ->
            + achieve("turn"("self")) onlyIf {
                "aligned"((1..gridDimension).map { cell(symbol = symbol) }) and "turn"("self").selfSourced
            } then {
                iact("print"(if (symbol == mySymbol) "I won!" else "I lost!"))
                iact("stop")
            }
        }
        for (winningLine in allPossibleCombinationsOf(cell(X, Y, "e"), cell(symbol = mySymbol), cell(symbol = "e"), gridDimension - 1 )) {
            + achieve("turn"("self")) onlyIf {
                "aligned"(winningLine) and "turn"("self").selfSourced
            } then {
                act("put"(X, Y, mySymbol))
                update("turn"("other").selfSourced)
            }
        }
        + achieve("turn"("self")) onlyIf {
            cell(X, Y, "e").fromPercept and "turn"("self").selfSourced
        } then {
            act("put"(X, Y, mySymbol))
            update("turn"("other").selfSourced)
        }
    }
}

fun main() {
    val system = ticTacToe(5)
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
