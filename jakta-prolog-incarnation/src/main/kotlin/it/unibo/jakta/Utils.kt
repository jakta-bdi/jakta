package it.unibo.jakta

import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var

context(substitution: Substitution)
val Var.value: Term
    get() = substitution[this] ?: error { "Variable $this is not ground in the substitution $substitution" }

fun Var.valueFromContext(substitution: Substitution): Term = context(substitution) { this.value }

fun Term.asInt(): Int = this.asInteger()?.value?.toInt() ?: error { "Term $this is not an integer" }
