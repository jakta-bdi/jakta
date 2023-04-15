package it.unibo.jakta.agents.bdi

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.visitors.DefaultTermVisitor

object Prolog2Jakta : DefaultTermVisitor<Term>() {

    override fun defaultValue(term: Term): Term = term

    override fun visitStruct(term: Struct): Term {
        return when {
            term.arity == 2 && term.functor == "," ->
                Struct.of("&", term.args.map { it.accept(this) })
            term.arity == 2 && term.functor == ";" ->
                Struct.of("|", term.args.map { it.accept(this) })
            term.arity == 1 && term.functor == "not" ->
                Struct.of("~", term.args.map { it.accept(this) })
            else -> super.visitStruct(term)
        }
    }
}
