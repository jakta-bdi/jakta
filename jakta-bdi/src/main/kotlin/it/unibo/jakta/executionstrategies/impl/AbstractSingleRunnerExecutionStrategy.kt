package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.executionstrategies.ExecutionStrategy

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
