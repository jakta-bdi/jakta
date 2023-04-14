package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.dsl.MasScope
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.selfSourced
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.aligned
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.allPossibleCombinationsOf
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.cell
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.turn
import io.github.anitvam.agents.bdi.dsl.mas
import it.unibo.tuprolog.dsl.LogicProgrammingScope

fun BeliefsScope.alignment(name: String, dx: Int, dy: Int) {
    rule(name(listOf(cell(X, Y, Z))) impliedBy cell(X, Y, Z))
    rule(name(listFrom(cell(A, B, C), cell(X, Y, Z), last = W)) .impliedBy(
        cell(A, B, C),
        cell(X, Y, Z),
        (X - A) arithEq dx,
        (Y - B) arithEq dy,
        name(listFrom(cell(X, Y, Z), last = W)).selfSourced
    ))

}

fun ticTacToe(n: Int = 3) = mas {
    environment {
        from(GridEnvironment(n))
        actions {
            action(Put)
            action("printGrid", 0) {
                for(row in environment.data["grid"] as Array<*>) {
                    for(cell in row as CharArray) {
                        print("$cell \t")
                    }
                    println()
                }
            }
        }
    }
    generatePlayer("x", "o", n)
    generatePlayer("o", "x", n)
}

fun LogicProgrammingScope.generateCombinations(mySymbol: String, gridDimension: Int) =
    allPossibleCombinationsOf(cell(X, Y, "e"), cell(`_`, `_`, mySymbol), cell(`_`, `_`, "e"), gridDimension - 1 )


fun MasScope.generatePlayer(mySymbol: String, otherSymbol: String, gridDimension: Int) = agent("$mySymbol-agent") {
    beliefs {
        alignment("vertical", dx = 0, dy = 1)
        alignment("horizontal", dx = 1, dy = 0)
        alignment("diagonal", dx = 1, dy = 1)
        alignment("antidiagonal", dx = 1, dy = -1)
        for (direction in arrayOf("vertical", "horizontal", "diagonal", "antidiagonal")) {
            rule("aligned"(L) impliedBy direction(L).selfSourced)
        }
    }
    goals { if (mySymbol=="x") achieve("play") }
    plans {
        arrayOf(mySymbol, otherSymbol).map { symbol ->
            + achieve("play") onlyIf {
                aligned((1..gridDimension).map { cell(`_`, `_`, symbol) })
            } then {
                iact("print"(if (symbol == mySymbol) "I won!" else "I lost!"))
                iact("stop")
            }
        }
        for (winningLine in generateCombinations(mySymbol, gridDimension)) {
            + achieve("play") onlyIf {
                aligned(winningLine)
            } then {
                execute("put"(X, Y, mySymbol, otherSymbol))
                execute("printGrid")
            }
        }
        + achieve("play") onlyIf {
            cell(X, Y, "e")
        } then {
            execute("put"(X, Y, mySymbol, otherSymbol))
            execute("printGrid")
        }

        + turn("$mySymbol-agent") then {
            - turn("$mySymbol-agent")
            achieve("play")
        }

    }
}

fun main() {
    val system = ticTacToe(3)
    for (agent in system.agents) {
        println("% ${agent.name}")
        for (plan in agent.context.planLibrary.plans) {
            //val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
            println("+!${plan.trigger.value} " +
                    ": ${plan.guard} " +
                    "<- ${plan.goals.joinToString("; "){ it.value.toString() }}")
        }
    }

   //system.start()
}
