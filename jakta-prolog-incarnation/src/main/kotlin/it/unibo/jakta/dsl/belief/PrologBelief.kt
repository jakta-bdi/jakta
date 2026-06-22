package it.unibo.jakta.dsl.belief

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.dsl.goal.PrologGoal
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologBelief = Rule

private fun PrologBelief.matchBelief(belief: Fact): Substitution? = when (val substitution = this mguWith belief) {
    is Substitution.Fail -> null
    else -> substitution
}

@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun PrologBelief.matching(block: JaktaLogicProgrammingScope.() -> Struct): Substitution? =
    matchBelief(contextualBeliefQuery(block))

fun initialBelief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    JaktaLogicProgrammingScope().block().also {
        requirePredicate(it) { "Belief must be a predicate, but got $it" }
        requireGround(it) { "Belief must be ground, but got $it" }
    },
)

context(scope: JaktaLogicProgrammingScope)
fun belief(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    scope.block().also {
        requirePredicate(it) { "Belief must be a predicate, but got $it" }
        requireGround(it) { "Belief must be ground, but got $it" }
    },
)

fun inferenceRule(block: JaktaLogicProgrammingScope.() -> Rule): Rule = JaktaLogicProgrammingScope().block()

fun JaktaLogicProgrammingScope.inferenceRule(block: JaktaLogicProgrammingScope.() -> Rule): Rule = this.block()

fun beliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    JaktaLogicProgrammingScope().block().also {
        requirePredicate(it) { "Belief query must be a predicate, but got $it" }
    },
)

context(scope: JaktaLogicProgrammingScope)
private fun contextualBeliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Fact = Fact.of(
    scope.block().also {
        requirePredicate(it) { "Belief query must be a predicate, but got $it" }
    },
)
