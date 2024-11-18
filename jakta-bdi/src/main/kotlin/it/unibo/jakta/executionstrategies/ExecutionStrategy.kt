package it.unibo.jakta.executionstrategies

import it.unibo.jakta.ASAgent
import it.unibo.jakta.Mas
import it.unibo.jakta.executionstrategies.impl.DiscreteEventExecutionImpl
import it.unibo.jakta.executionstrategies.impl.DiscreteTimeExecutionImpl
import it.unibo.jakta.executionstrategies.impl.OneThreadPerAgentImpl
import it.unibo.jakta.executionstrategies.impl.OneThreadPerMasImpl

interface ExecutionStrategy {
    fun dispatch(mas: Mas, debugEnabled: Boolean)

    fun spawnAgent(agent: ASAgent)

    fun removeAgent(agentName: String)

    companion object {
        fun oneThreadPerAgent(): ExecutionStrategy = OneThreadPerAgentImpl()

        fun oneThreadPerMas(): ExecutionStrategy = OneThreadPerMasImpl()

        fun discreteEventExecution(): ExecutionStrategy = DiscreteEventExecutionImpl()

        fun discreteTimeExecution(): ExecutionStrategy = DiscreteTimeExecutionImpl()
    }
}
