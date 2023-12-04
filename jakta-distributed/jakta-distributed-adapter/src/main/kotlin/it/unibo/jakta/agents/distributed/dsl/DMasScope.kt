package it.unibo.jakta.agents.distributed.dsl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.bdi.dsl.AgentScope
import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.dsl.environment.EnvironmentScope
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.broker.embedded.EmbeddedBroker
import it.unibo.jakta.agents.distributed.dmas.DMas
import it.unibo.jakta.agents.distributed.dmas.impl.DMasWithEmbeddedBroker
import it.unibo.jakta.agents.distributed.network.Network

class DMasScope : Builder<DMas> {
    private var env: Environment = Environment.of()
    private var agents: List<Agent> = emptyList()
    private var executionStrategy = ExecutionStrategy.oneThreadPerAgent()
    private var network: Network = dummyNetwork()
    private var services: Iterable<RemoteService> = emptyList()
    private var embeddedBroker: EmbeddedBroker? = null

    fun environment(f: EnvironmentScope.() -> Unit): DMasScope {
        env = EnvironmentScope().also(f).build()
        return this
    }

    fun environment(environment: Environment): DMasScope {
        env = environment
        return this
    }

    fun agent(name: String, f: AgentScope.() -> Unit): DMasScope {
        agents += AgentScope(name).also(f).build()
        return this
    }

    fun executionStrategy(f: () -> ExecutionStrategy): DMasScope {
        executionStrategy = f()
        return this
    }

    fun network(f: () -> Network): DMasScope {
        this.network = f()
        return this
    }

    fun services(f: () -> Iterable<RemoteService>): DMasScope {
        this.services = f()
        return this
    }

    fun service(f: RemoteServiceScope.() -> Unit): DMasScope {
        services += RemoteServiceScope().also(f).build()
        return this
    }

    fun embeddedBroker(f: () -> EmbeddedBroker): DMasScope {
        this.embeddedBroker = f()
        return this
    }

    override fun build(): DMas {
        return if (embeddedBroker != null) {
            DMasWithEmbeddedBroker(
                env,
                agents,
                executionStrategy,
                network,
                services,
                embeddedBroker!!,
            )
        } else {
            DMas.of(
                executionStrategy,
                env,
                agents,
                network,
                services,
            )
        }
    }
}

private fun dummyNetwork(): Network =
    object : Network {
        override suspend fun subscribe(remoteService: RemoteService) { }

        override suspend fun send(event: SendMessage) { }

        override suspend fun broadcast(event: BroadcastMessage) { }

        override fun getMessagesAsEnvironmentChanges(): Iterable<EnvironmentChange> =
            emptyList<EnvironmentChange>().asIterable()

        override fun getDisconnections(): Iterable<RemoteService> =
            emptyList<RemoteService>().asIterable()
    }
