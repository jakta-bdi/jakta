package it.unibo.jakta.dsl.goal

import it.unibo.jakta.dsl.JaktaDSL
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import it.unibo.jakta.logic.specialFunctors
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologGoal = Fact

@JaktaDSL
fun PrologGoal.matching(goal: PrologGoal): Substitution? = when (val substitution = this mguWith goal) {
    is Substitution.Fail -> null
    else -> substitution
}

fun initialGoal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal = Fact.of(
    JaktaLogicProgrammingScope().block().also {
        require(it.functor !in specialFunctors) { "Goal must be a predicate" }
    },
).also {
    require(it.isGround) { "Goal must be ground" }
}

context(scope: JaktaLogicProgrammingScope)
fun goal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal = Fact.of(
    scope.block().also {
        require(it.functor !in specialFunctors) { "Goal must be a predicate" }
    },
).also {
    require(it.isGround) { "Goal must be ground" }
}

context(scope: JaktaLogicProgrammingScope)
fun goalQuery(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal = Fact.of(scope.block())
