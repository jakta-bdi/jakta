package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.Jakta
import io.github.anitvam.agents.bdi.actions.InternalActions
import io.github.anitvam.agents.bdi.actions.InternalActions.Print
import io.github.anitvam.agents.bdi.dsl.MasScope
import io.github.anitvam.agents.bdi.dsl.beliefs.BeliefsScope
import io.github.anitvam.agents.bdi.dsl.beliefs.fromPercept
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.aligned
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.allPossibleCombinationsOf
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.antidiagonal
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.cell
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.diagonal
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.e
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.horizontal
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.invoke
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.o
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.turn
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.vertical
import io.github.anitvam.agents.bdi.dsl.examples.tris.TicTacToeLiterals.x
import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.dsl.plans.BodyScope
import io.github.anitvam.agents.bdi.dsl.plans.PlansScope
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.operators.OperatorSet

fun BeliefsScope.alignment(name: String, dx: Int, dy: Int) {
    val first = cell(A, B, C).fromPercept
    val second = cell(X, Y, Z).fromPercept
    rule { name(listOf(second)) impliedBy second }
    rule {
        name(listFrom(first, second, last = W)) .impliedBy(
            first,
            second,
            (X - A) arithEq dx,
            (Y - B) arithEq dy,
            name(listFrom(second, last = W))
        )
    }
}

fun ticTacToe(n: Int = 3) = mas {
    require(n > 0)
    environment {
        from(GridEnvironment(n))
        actions { action(Put) }
    }
    player(mySymbol=o, otherSymbol=x, gridSize=n)
    player(mySymbol=x, otherSymbol=o, gridSize=n)
}

fun MasScope.player(mySymbol: String, otherSymbol: String, gridSize: Int) = agent("${mySymbol}-agent") {
    beliefs {
        alignment(vertical, dx=0, dy=1)
        alignment(horizontal, dx=1, dy=0)
        alignment(diagonal, dx=1, dy=1)
        alignment(antidiagonal, dx=1, dy=-1)
        for (direction in arrayOf(vertical, horizontal, diagonal, antidiagonal)) {
            rule { aligned(L) impliedBy direction(L) }
        }
    }
    plans {
        detectVictory(mySymbol, gridSize)                           // plan 1
        detectDefeat(mySymbol, otherSymbol, gridSize)               // plan 2
        makeWinningMove(mySymbol, gridSize)                         // plan 3
        preventOtherFromWinning(mySymbol, otherSymbol, gridSize)    // plan 4
        randomMove(mySymbol)                                        // plan 5
    }
}

fun PlansScope.detectVictory(mySymbol: String, gridSize: Int) =
    detectLine(mySymbol, mySymbol, gridSize) { Print("I won!") }
fun PlansScope.detectDefeat(mySymbol: String, otherSymbol: String, gridSize: Int) =
    detectLine(mySymbol, otherSymbol, gridSize) { Print("I lost!") }

fun PlansScope.detectLine(mySymbol: String, symbol: String, size: Int, action: BodyScope.() -> Unit) =
    +turn(mySymbol) onlyIf { aligned((1..size).map { cell(symbol) }) } then(action)

fun PlansScope.makeWinningMove(mySymbol: String, gridSize: Int, symbol: String = mySymbol) {
    for (winningLine in allPossibleCombinationsOf(cell(X, Y, e), cell(symbol), gridSize - 1)) {
        +turn(mySymbol) onlyIf { aligned(winningLine) } then { Put(X, Y, mySymbol) }
    }
}

fun PlansScope.preventOtherFromWinning(mySymbol: String, otherSymbol: String, gridSize: Int) =
    makeWinningMove(mySymbol, gridSize, otherSymbol)

fun PlansScope.randomMove(mySymbol: String) =
    +turn(mySymbol) onlyIf { cell(X, Y, e) } then { Put(X, Y, mySymbol) }


fun main() {
    val system = ticTacToe(3)
    for (agent in system.agents) {
        println("% ${agent.name}")
        for (belief in agent.context.beliefBase) {
            val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
            println(formatter.format(belief.rule))
        }
        for (plan in agent.context.planLibrary.plans) {
            val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
            println("+!${formatter.format(plan.trigger.value)} " +
                    ": ${formatter.format(plan.guard)} " +
                    "<- ${plan.goals.joinToString("; "){ formatter.format(it.value) }}")
        }
    }
//    system.start()
}
