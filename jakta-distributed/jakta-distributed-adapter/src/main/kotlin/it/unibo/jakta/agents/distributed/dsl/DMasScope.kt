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
import it.unibo.jakta.agents.distributed.dmas.DMas
import it.unibo.jakta.agents.distributed.network.Network

class DMasScope : Builder<DMas> {
    var env: Environment = Environment.of()
    var agents: List<Agent> = emptyList()
    var executionStrategy = ExecutionStrategy.oneThreadPerAgent()
    var network: Network = dummyNetwork()
    var services: Iterable<RemoteService> = emptyList()

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

    fun network(network: Network): DMasScope {
        this.network = network
        return this
    }

    fun services(services: Iterable<RemoteService>): DMasScope {
        this.services = services
        return this
    }

    override fun build(): DMas = DMas.of(executionStrategy, env, agents, network, services)
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
