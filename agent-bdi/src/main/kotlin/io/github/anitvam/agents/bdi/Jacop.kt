package io.github.anitvam.agents.bdi

import it.unibo.tuprolog.core.Clause
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.operators.Operator
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.core.operators.Specifier
import it.unibo.tuprolog.core.parsing.TermParser

object Jacop {

    // fun convert1(struct: Struct): Term = struct.accept(jasonTo2p)

    private val customOperators = OperatorSet(
        Operator("&", Specifier.XFY, 1000),
        Operator("|", Specifier.XFY, 1100),
        Operator("~", Specifier.FX, 900),
    )

    private val parser = TermParser.withOperators(OperatorSet.DEFAULT + customOperators)

    fun parseStruct(string: String): Struct = parser.parseStruct(string)
    fun parseClause(string: String): Clause = parser.parseClause(string)

//    private val jasonTo2p = object : DefaultTermVisitor<Term>() {
//
//        override fun defaultValue(term: Term): Term = term
//
//        override fun visitStruct(term: Struct): Term {
//            return when {
//                term.arity == 2 && term.functor == "&" ->
//                    Struct.of(",", term.args.map { it.accept(this) })
//                term.arity == 2 && term.functor == "|" ->
//                    Struct.of(";", term.args.map { it.accept(this) })
//                term.arity == 1 && term.functor == "~" ->
//                    Struct.of("not", term.args.map { it.accept(this) })
//                else -> super.visitStruct(term)
//            }
//        }
//    }
}
