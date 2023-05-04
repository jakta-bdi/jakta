package it.unibo.jakta.agents.bdi

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.impl.MasImpl

interface Mas {
    val environment: Environment
    val agents: Iterable<Agent>
    val executionStrategy: ExecutionStrategy

    fun start(debugEnabled: Boolean = false)

    fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>)

    companion object {
        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agent: Agent,
            vararg agents: Agent,
        ): Mas =
            of(executionStrategy, environment, agents.asIterable() + agent)

        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agents: Iterable<Agent>,
        ): Mas = MasImpl(executionStrategy, environment, agents)
    }
}
