package it.unibo.jakta.agents.bdi.dsl.plans

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class GuardScope(private val lpScope: Scope) : LogicProgrammingScope by LogicProgrammingScope.of(lpScope)
