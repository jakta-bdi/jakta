package it.unibo.jakta.agents.distributed.dmas.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.network.Network

internal class DMasImpl(
    override val executionStrategy: ExecutionStrategy,
    override var environment: Environment,
    override var agents: Iterable<Agent>,
    override val services: Iterable<RemoteService>,
    override val network: Network,
) : AbstractDMas(
    executionStrategy,
    environment,
    agents,
    services,
    network,
) {
    init {
        agents.forEach { environment = environment.addAgent(it) }
    }
}
