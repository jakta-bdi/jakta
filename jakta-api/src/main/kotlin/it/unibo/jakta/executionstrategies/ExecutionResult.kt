package it.unibo.jakta.executionstrategies

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.effects.EnvironmentChange

data class ExecutionResult<Belief : Any, Query : Any, Response>(
    val newAgentContext: Agent<Belief, Query, Response>,
    val environmentEffects: Iterable<EnvironmentChange<Belief, Query, Response>> = listOf(),
)
