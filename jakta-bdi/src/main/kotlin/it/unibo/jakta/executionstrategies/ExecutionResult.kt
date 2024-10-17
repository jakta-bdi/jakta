package it.unibo.jakta.executionstrategies

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.context.AgentContext

class ExecutionResult(
    val newAgentContext: AgentContext,
    val environmentEffects: Iterable<EnvironmentChange> = listOf(),
)
