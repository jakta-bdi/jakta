package it.unibo.jakta.agents.bdi.executionstrategies

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.executionstrategies.impl.DiscreteEventExecutionImpl
import it.unibo.jakta.agents.bdi.executionstrategies.impl.DiscreteTimeExecutionImpl
import it.unibo.jakta.agents.bdi.executionstrategies.impl.OneThreadPerAgentImpl
import it.unibo.jakta.agents.bdi.executionstrategies.impl.OneThreadPerMasImpl

interface ExecutionStrategy {
    fun dispatch(
        mas: Mas,
        debugEnabled: Boolean,
    )

    fun spawnAgent(agent: Agent)

    fun removeAgent(agentName: String)

    companion object {
        fun oneThreadPerAgent(): ExecutionStrategy = OneThreadPerAgentImpl()

        fun oneThreadPerMas(): ExecutionStrategy = OneThreadPerMasImpl()

        fun discreteEventExecution(): ExecutionStrategy = DiscreteEventExecutionImpl()

        fun discreteTimeExecution(): ExecutionStrategy = DiscreteTimeExecutionImpl()
    }
}
