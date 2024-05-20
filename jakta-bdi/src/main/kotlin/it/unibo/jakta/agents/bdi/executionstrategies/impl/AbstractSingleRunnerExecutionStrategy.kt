package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy

abstract class AbstractSingleRunnerExecutionStrategy : ExecutionStrategy {

    protected val synchronizedAgents = SynchronizedAgents()

    override fun spawnAgent(agent: Agent) {
        synchronizedAgents.addAgent(agent)
    }

    override fun removeAgent(agentName: String) {
        synchronizedAgents.removeAgent(agentName)
    }

    class SynchronizedAgents {
        private var agents: Map<Agent, AgentLifecycle> = emptyMap()

        @Synchronized
        fun addAgent(agent: Agent) {
            agents = agents + (agent to AgentLifecycle.newLifecycleFor(agent))
        }

        @Synchronized
        fun removeAgent(agentName: String) {
            agents = agents.filter { it.key.name != agentName }
        }

        @Synchronized
        fun getAgents(): Map<Agent, AgentLifecycle> = agents
    }
}
