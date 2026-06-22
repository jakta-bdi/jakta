package it.unibo.jakta.dsl.goal

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.requireGround
import it.unibo.jakta.logic.requirePredicate
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologGoal = Struct

private fun PrologGoal.matchGoal(goal: PrologGoal): Substitution? = when (val substitution = this mguWith goal) {
    is Substitution.Fail -> null
    else -> substitution
}

@JaktaDSL
context(scope: JaktaLogicProgrammingScope)
fun PrologGoal.matching(block: JaktaLogicProgrammingScope.() -> Struct): Substitution? = matchGoal(goalQuery(block))

fun initialGoal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    JaktaLogicProgrammingScope().block().also {
        requirePredicate(it) { "Initial goal must be a predicate, but got $it" }
        requireGround(it) { "Goal must be ground, but got $it" }
    }

context(scope: JaktaLogicProgrammingScope, substitution: Substitution)
fun goal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    (scope.block().apply(substitution) as Struct).also {
        requirePredicate(it) { "Goal must be a predicate, but got $it" }
        requireGround(it) { "Goal must be ground, but got $it" }
    }


context(scope: JaktaLogicProgrammingScope)
private fun goalQuery(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    scope.block().also {
        requirePredicate(it) { "Goal query must be a predicate, but got $it" }
    }
