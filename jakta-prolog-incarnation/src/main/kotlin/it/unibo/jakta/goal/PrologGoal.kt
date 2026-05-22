package it.unibo.jakta.goal

import it.unibo.jakta.dsl.JaktaLogicProgrammingScope
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologGoal = Fact

fun PrologGoal.matching(goal: PrologGoal): Substitution? =
    when (val substitution = this mguWith goal) {
        is Substitution.Fail -> null
        else -> substitution
    }

data class SubstituedTerm(val term: Term)

context(substitution: Substitution )
val Var.value: SubstituedTerm?
    get() = substitution[this]?.let { SubstituedTerm(it) }

fun SubstituedTerm.asInt(): Int? = this.term.asInteger()?.value?.toInt()

context(substitution: Substitution )
fun SubstituedTerm.asString(): String? = this.term.asAtom()?.value

context(substitution: Substitution )
fun SubstituedTerm.asBoolean(): Boolean? = this.term.asTruth()?.isTrue

context(substitution: Substitution )
fun SubstituedTerm.asDouble(): Double? = this.term.asReal()?.value?.toDouble()
