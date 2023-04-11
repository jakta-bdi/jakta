package io.github.anitvam.agents.bdi.dsl.examples.tris

import it.unibo.tuprolog.dsl.LogicProgrammingScope

object TrisLiterals {
    fun LogicProgrammingScope.cell(
        x: Any = `_`,
        y: Any = `_`,
        symbol: Any
    ) = structOf("cell", x.toTerm(), y.toTerm(), symbol.toTerm())
}