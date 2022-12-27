package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.impl.MasImpl

interface Mas {
    val environment: Environment
    val agents: Iterable<Agent>
    val executionStrategy: ExecutionStrategy

    fun start()

    companion object {
        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agent: Agent,
            vararg agents: Agent,
        ): Mas = MasImpl(executionStrategy, environment, agents.asIterable() + agent)
    }
}
