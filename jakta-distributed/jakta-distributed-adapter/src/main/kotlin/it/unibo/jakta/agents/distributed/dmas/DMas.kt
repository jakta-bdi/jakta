package it.unibo.jakta.agents.distributed.dmas

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.broker.embedded.EmbeddedBroker
import it.unibo.jakta.agents.distributed.dmas.impl.DMasImpl
import it.unibo.jakta.agents.distributed.dmas.impl.DMasWithEmbeddedBroker
import it.unibo.jakta.agents.distributed.network.Network

/**
 * A DistributedMas is a Mas with networking capabilities: it can send and receive messages from other DMas' over the
 * network.
 */
interface DMas : Mas {
    val network: Network
    val services: Iterable<RemoteService>

    companion object {
        fun of(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agents: Iterable<Agent>,
            network: Network,
            services: Iterable<RemoteService>,
        ): DMas = DMasImpl(executionStrategy, environment, agents, services, network)

        fun fromWebSocketNetwork(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agents: Iterable<Agent>,
            services: Iterable<RemoteService>,
            host: String,
            port: Int,
        ): DMas = DMasImpl(executionStrategy, environment, agents, services, Network.websocketNetwork(host, port))

        fun withEmbeddedBroker(
            executionStrategy: ExecutionStrategy,
            environment: Environment,
            agents: Iterable<Agent>,
            services: Iterable<RemoteService>,
            port: Int = 8080,
        ): DMas = DMasWithEmbeddedBroker(
            environment,
            agents,
            executionStrategy,
            Network.websocketNetwork("localhost", port),
            services,
            EmbeddedBroker(port),
        )
    }
}
