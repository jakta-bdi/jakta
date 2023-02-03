package io.github.anitvam.agents.bdi.dsl.plans

import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.dsl.LogicProgrammingScope

// TODO wait for gc release (ping him)
class GuardScope(private val scope: Scope) : LogicProgrammingScope by LogicProgrammingScope.empty()
