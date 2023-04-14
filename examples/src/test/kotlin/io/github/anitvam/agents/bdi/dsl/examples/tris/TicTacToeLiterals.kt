package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.dsl.beliefs.fromPercept
import io.github.anitvam.agents.bdi.dsl.beliefs.fromSelf
import io.github.anitvam.agents.bdi.dsl.examples.OwnName
import io.github.anitvam.agents.bdi.dsl.plans.BodyScope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import it.unibo.tuprolog.utils.permutations

object TicTacToeLiterals {

    fun LogicProgrammingScope.cell(
        x: Any?,
        y: Any?,
        symbol: Any
    ) = structOf("cell", (x ?: `_`).toTerm(), (y ?: `_`).toTerm(), symbol.toTerm()).fromPercept

    fun LogicProgrammingScope.cell(symbol: Any) = cell(null, null, symbol)

    fun LogicProgrammingScope.turn(
        symbol: Any
    ) = structOf("turn", symbol.toTerm()).fromPercept

    fun LogicProgrammingScope.aligned(
        symbol: Any
    ) = structOf("aligned", symbol.toTerm()).fromSelf

    fun allPossibleCombinationsOf(cell: Struct, other: Struct, repetitions: Int): Sequence<List<Struct>> =
        (1..repetitions).map { other }
            .plus(cell)
            .permutations()
            .distinct()
            .map { permutation -> permutation.map { it.freshCopy() } }

    context (BodyScope)
    operator fun <T : ExternalAction> T.invoke(arg: Any, vararg args: Any) {
        execute(signature.name(arg, *args))
    }

    context (BodyScope)
    operator fun <T : InternalAction> T.invoke(arg: Any, vararg args: Any) {
        iact(signature.name(arg, *args))
    }

    val vertical by OwnName
    val horizontal by OwnName
    val diagonal by OwnName
    val antidiagonal by OwnName
    val x by OwnName
    val e by OwnName
    val o by OwnName

}
