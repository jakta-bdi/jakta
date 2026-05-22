package it.unibo.jakta.dsl

import it.unibo.jakta.belief.specialFunctors
import it.unibo.jakta.goal.PrologGoal
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Struct

context(scope: JaktaLogicProgrammingScope)
fun goal(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    Fact.of(scope.block().also {
        require(it.functor !in specialFunctors) { "Goal must be a predicate" }
    }).also {
        require(it.isGround) { "Goal must be ground" }
    }


context(scope: JaktaLogicProgrammingScope)
fun goalQuery(block: JaktaLogicProgrammingScope.() -> Struct): PrologGoal =
    Fact.of(scope.block())
