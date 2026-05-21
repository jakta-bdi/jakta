package it.unibo.jakta.dsl

import it.unibo.tuprolog.core.Struct

fun prologGoal(goal: JaktaLogicProgrammingScope.() -> Struct): Struct =
    JaktaLogicProgrammingScope().let(goal)
