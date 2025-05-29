package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.executionstrategies.ExecutionStrategy

abstract class AbstractSingleRunnerExecutionStrategy : ExecutionStrategy {

    protected val synchronizedAgents = SynchronizedAgents()

    override fun spawnAgent(agentLC: AgentLifecycle) {
        synchronizedAgents.addAgent(agentLC)
    }

    override fun removeAgent(agentName: String) {
        synchronizedAgents.removeAgent(agentName)
    }

    class SynchronizedAgents {
        private var agents: List<AgentLifecycle> = listOf()

        @Synchronized
        fun addAgent(agentLC: AgentLifecycle) {
            agents = agents + agentLC
        }

        @Synchronized
        fun removeAgent(agentName: String) {
            agents = agents.filter { it.agent.context.agentName != agentName }
        }

        @Synchronized
        fun getAgents(): List<AgentLifecycle> = agents
    }
}
