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

    fun allDistinctPermutationsOf(cell: Struct, other: Struct, otherRepetitions: Int): Sequence<List<Struct>> =
        ((1..otherRepetitions).map { other } + cell).permutations().distinct()

    fun allDistinctPermutationsOf(cell: Struct, vararg others: Struct): Sequence<List<Struct>> =
        (others.asList() + cell).permutations().distinct()
}
