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

    fun allDistinctPermutationsOf(cell: Struct, other: Struct, repetitions: Int): Sequence<List<Struct>> =
        ((1..repetitions).map { other } + cell).permutations().distinct()
}
