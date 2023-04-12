package io.github.anitvam.agents.bdi.dsl.examples.tris

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope
import it.unibo.tuprolog.utils.permutations

object TicTacToeLiterals {
    fun LogicProgrammingScope.cell(
        x: Any = `_`,
        y: Any = `_`,
        symbol: Any
    ) = structOf("cell", x.toTerm(), y.toTerm(), symbol.toTerm())

    fun allPossibleCombinationsOf(cell: Struct, otherOccupied: Struct, otherFree: Struct, repetitions: Int): List<List<Struct>> =
        (1..repetitions).flatMap {
            ((1 .. it).map { otherOccupied } + (1..(repetitions - it)).map{ otherFree } + cell).permutations().distinct()
        }

}
