package it.unibo.jakta.agents.bdi.executionstrategies

import it.unibo.jakta.agents.bdi.context.AgentContext
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange

class ExecutionResult(
    val newAgentContext: AgentContext,
    val environmentEffects: Iterable<EnvironmentChange> = listOf(),
)
