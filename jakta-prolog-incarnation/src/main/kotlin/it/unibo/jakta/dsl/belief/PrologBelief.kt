package it.unibo.jakta.dsl.belief

import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologBelief = Rule

fun PrologBelief.matching(belief: Fact): Substitution? = when (val substitution = this mguWith belief) {
    is Substitution.Fail -> null
    else -> substitution
}

fun belief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(JaktaLogicProgrammingScope().block()).also {
    require(it.isGround) { "Belief must be ground" }
}

fun rule(block: JaktaLogicProgrammingScope.() -> Struct): Rule = Rule.of(JaktaLogicProgrammingScope().block())

context(scope: JaktaLogicProgrammingScope)
fun beliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(scope.block())
