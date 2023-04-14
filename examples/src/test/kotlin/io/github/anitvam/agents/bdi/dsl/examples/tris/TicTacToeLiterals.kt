package io.github.anitvam.agents.bdi.dsl.examples.tris

import io.github.anitvam.agents.bdi.dsl.beliefs.fromPercept
import io.github.anitvam.agents.bdi.dsl.beliefs.selfSourced
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import it.unibo.tuprolog.utils.permutations

object TicTacToeLiterals {
    fun LogicProgrammingScope.cell(
        x: Any? = null,
        y: Any? = null,
        symbol: Any
    ) = structOf("cell", (x ?: `_`).toTerm(), (y ?: `_`).toTerm(), symbol.toTerm()).fromPercept

    fun LogicProgrammingScope.turn(
        symbol: Any
    ) = structOf("turn", symbol.toTerm()).fromPercept

    fun LogicProgrammingScope.aligned(
        symbol: Any
    ) = structOf("aligned", symbol.toTerm()).selfSourced
    fun allPossibleCombinationsOf(cell: Struct, otherOccupied: Struct, otherFree: Struct, repetitions: Int): List<List<Struct>> =
        (1..repetitions).flatMap {
            ((1 .. it).map { otherOccupied } + (1..(repetitions - it)).map{ otherFree } + cell).permutations().distinct()
        }

}
