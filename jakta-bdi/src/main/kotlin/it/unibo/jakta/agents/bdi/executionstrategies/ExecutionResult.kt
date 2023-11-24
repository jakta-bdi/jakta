package it.unibo.jakta.agents.bdi.executionstrategies

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.context.AgentContext

class ExecutionResult(
    val newAgentContext: AgentContext,
    val environmentEffects: Iterable<EnvironmentChange> = listOf(),
)
