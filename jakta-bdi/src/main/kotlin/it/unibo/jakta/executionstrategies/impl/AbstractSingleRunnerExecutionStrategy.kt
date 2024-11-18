package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.executionstrategies.ExecutionStrategy

abstract class AbstractSingleRunnerExecutionStrategy : ExecutionStrategy {

    protected val synchronizedAgents = SynchronizedAgents()

    override fun spawnAgent(agent: ASAgent) {
        synchronizedAgents.addAgent(agent)
    }

    override fun removeAgent(agentName: String) {
        synchronizedAgents.removeAgent(agentName)
    }

    class SynchronizedAgents {
        private var agents: Map<ASAgent, AgentLifecycle> = emptyMap()

        @Synchronized
        fun addAgent(agent: ASAgent) {
            agents = agents + (agent to AgentLifecycle.newLifecycleFor(agent))
        }

        @Synchronized
        fun removeAgent(agentName: String) {
            agents = agents.filter { it.key.name != agentName }
        }

        @Synchronized
        fun getAgents(): Map<ASAgent, AgentLifecycle> = agents
    }
}
