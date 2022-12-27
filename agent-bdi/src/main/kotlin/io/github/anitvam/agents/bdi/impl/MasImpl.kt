package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.ExecutionStrategy
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.environment.Environment

internal class MasImpl(
    override val executionStrategy: ExecutionStrategy,
    override val environment: Environment,
    override val agents: Iterable<Agent>,
) : Mas {
    init {
        agents.forEach { environment.addAgent(it.agentID) }
    }

    override fun start() = agents.forEach { executionStrategy.dispatch(it, environment).run() }
}
