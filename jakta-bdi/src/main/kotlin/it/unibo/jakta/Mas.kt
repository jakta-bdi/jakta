package it.unibo.jakta

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.impl.MasImpl

interface Mas {
    val environment: BasicEnvironment
    val agents: Iterable<ASAgent>
    val executionStrategy: ExecutionStrategy

    fun start(debugEnabled: Boolean = false)

    fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>)

    companion object {
        fun of(
            executionStrategy: ExecutionStrategy,
            environment: BasicEnvironment,
            agent: ASAgent,
            vararg agents: ASAgent,
        ): Mas =
            of(executionStrategy, environment, agents.asIterable() + agent)

        fun of(
            executionStrategy: ExecutionStrategy,
            environment: BasicEnvironment,
            agents: Iterable<ASAgent>,
        ): Mas = MasImpl(executionStrategy, environment, agents)
    }
}
