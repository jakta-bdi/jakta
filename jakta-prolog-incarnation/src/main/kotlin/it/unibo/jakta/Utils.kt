package it.unibo.jakta

import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.core.visitors.DefaultTermVisitor
import it.unibo.tuprolog.serialize.TermObjectifier

context(substitution: Substitution)
val Var.value: Term
    get() = substitution[this] ?: error { "Variable $this is not ground in the substitution $substitution" }

fun Var.valueFromContext(substitution: Substitution): Term = context(substitution) { this.value }

fun <T> Term.getAs(): T =
    this.accept(TermObjectifier.default) as? T ?: error { "Term $this cannot be cast to the expected type" }
