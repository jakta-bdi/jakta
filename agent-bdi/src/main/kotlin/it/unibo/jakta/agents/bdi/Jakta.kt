package it.unibo.jakta.agents.bdi

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.TermParser

object Jakta {

    // fun convert1(struct: Struct): Term = struct.accept(jasonTo2p)

    val operators = OperatorSet(
        Operator("&", Specifier.XFY, 1000),
        Operator("|", Specifier.XFY, 1100),
        Operator("~", Specifier.FX, 900),
    )

    private val parser = TermParser.withOperators(OperatorSet.DEFAULT + operators)

    fun parseStruct(string: String): Struct = parser.parseStruct(string)
    fun parseClause(string: String): Clause = parser.parseClause(string)
}
