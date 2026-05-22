package it.unibo.jakta.dsl

import it.unibo.jakta.belief.PrologBelief
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct

fun belief(block: JaktaLogicProgrammingScope.() -> Struct): Fact =
    Fact.of(JaktaLogicProgrammingScope().block()).also {
        require(it.isGround) { "Belief must be ground" }
    }

fun rule(block: JaktaLogicProgrammingScope.() -> Struct): Rule =
    Rule.of(JaktaLogicProgrammingScope().block())

context(scope: JaktaLogicProgrammingScope)
fun beliefQuery(block: JaktaLogicProgrammingScope.() -> Struct): Fact =
    Fact.of(scope.block())
