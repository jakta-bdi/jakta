package it.unibo.jakta.agents.distributed.dmas

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.distributed.dmas.impl.DMasImpl
import it.unibo.jakta.agents.distributed.network.Network
import it.unibo.jakta.agents.distributed.remoteagent.RemoteAgent

/**
 * A DistributedMas is a Mas with networking capabilities: it can send and receive messages from other DMas' over the
 * network.
 */
interface DMas : Mas {
    val network: Network
    val remoteAgents: Iterable<RemoteAgent>

    companion object {
        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agents: Iterable<Agent>,
            network: Network,
            remoteAgents: Iterable<RemoteAgent>,
        ): DMas = DMasImpl(executionStrategy, environment, agents, remoteAgents, network)
    }
}
