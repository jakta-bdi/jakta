package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.ASAgentLifecycle
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.executionstrategies.ExecutionStrategy

abstract class AbstractSingleRunnerExecutionStrategy: ExecutionStrategy {

    protected val synchronizedAgents = SynchronizedAgents()

    override fun spawnAgent(agentLC: ASAgentLifecycle) {
        synchronizedAgents.addAgent(agentLC)
    }

    override fun removeAgent(agentName: String) {
        synchronizedAgents.removeAgent(agentName)
    }

    class SynchronizedAgents{
        private var agents: List<ASAgentLifecycle> = listOf()

        @Synchronized
        fun addAgent(agentLC: ASAgentLifecycle) {
            agents = agents + agentLC
        }

        @Synchronized
        fun removeAgent(agentName: String) {
            agents = agents.filter { it.agent.context.agentName != agentName }
        }

        @Synchronized
        fun getAgents(): List<ASAgentLifecycle> = agents
    }
}
