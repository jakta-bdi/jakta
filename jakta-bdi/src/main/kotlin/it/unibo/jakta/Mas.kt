package it.unibo.jakta

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.impl.MasImpl

interface Mas {
    val environment: Environment
    val agents: Iterable<ASAgent>
    val executionStrategy: ExecutionStrategy

    fun start(debugEnabled: Boolean = false)

    fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>)

    companion object {
        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agent: ASAgent,
            vararg agents: ASAgent,
        ): Mas =
            of(executionStrategy, environment, agents.asIterable() + agent)

        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agents: Iterable<ASAgent>,
        ): Mas = MasImpl(executionStrategy, environment, agents)
    }
}
