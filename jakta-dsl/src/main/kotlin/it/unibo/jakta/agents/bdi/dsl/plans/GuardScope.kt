package it.unibo.jakta.agents.bdi.dsl.plans

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class GuardScope(private val scope: Scope) : LogicProgrammingScope by LogicProgrammingScope.of(scope)
