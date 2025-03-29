package it.unibo.jakta.agents.bdi.dsl.plans

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.dsl.jakta.JaktaLogicProgrammingScope

class GuardScope(
    private val lpScope: Scope,
) : JaktaLogicProgrammingScope by JaktaLogicProgrammingScope.of(lpScope)
