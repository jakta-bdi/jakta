package it.unibo.jakta.executionstrategies

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.effects.EnvironmentChange

data class ExecutionResult(
    val newAgentContext: ASAgent,
    val environmentEffects: Iterable<EnvironmentChange> = listOf(),
)
