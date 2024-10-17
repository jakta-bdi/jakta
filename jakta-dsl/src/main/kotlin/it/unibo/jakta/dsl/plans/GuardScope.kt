package it.unibo.jakta.dsl.plans

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.jakta.dsl.JaktaLogicProgrammingScope

class GuardScope(private val lpScope: Scope) : JaktaLogicProgrammingScope by JaktaLogicProgrammingScope.of(lpScope)
