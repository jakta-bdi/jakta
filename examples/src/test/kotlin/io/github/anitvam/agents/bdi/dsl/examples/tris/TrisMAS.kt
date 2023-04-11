package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.fromPercept
import io.github.anitvam.agents.bdi.dsl.examples.tris.TrisLiterals.cell
import io.github.anitvam.agents.bdi.dsl.mas
import it.unibo.tuprolog.utils.permutations

fun BeliefsScope.alignment(name: String, dx: Int, dy: Int) {
    rule { name(listOf("cell"(X, Y, S))) impliedBy ("cell"(X, Y, S).fromPercept) }
    rule {
        val second = "cell"(X, Y, S).fromPercept
        name(listFrom("cell"(A, B, C), "cell"(X, Y, S), last = W)) .impliedBy(
            "cell"(A, B, C).fromPercept,
            second,
            (X - A) arithEq dx,
            (Y - B) arithEq dy,
            name(listFrom(second, last = W))
        )
    }
}

fun ticTacToe(n: Int = 3) = mas {
    environment {
        from(TrisEnvironment())
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
        plans {
            + achieve("turn"("self")) onlyIf {
                "aligned"(listOf(cell(symbol="x"), cell(symbol="x"), cell(symbol="x")))
            } then {
                iact("print"("I won!"))
            }
            for (winningLines in permutations(listOf(cell(X, Y, "e"), cell(symbol="x"), cell(symbol="x"))).distinct()) {
                + achieve("turn"("self")) onlyIf {
                    "aligned"(winningLines)
                } then {
                    act("put"(X, Y, "x"))
                }
            }
            + achieve("turn"("self")) onlyIf {
                "cell"(X, Y, "e").fromPercept
            } then {
                act("put"(X, Y, "x"))
            }
        }
    }
}

