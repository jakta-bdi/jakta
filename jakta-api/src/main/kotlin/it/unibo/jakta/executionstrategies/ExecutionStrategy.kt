package it.unibo.jakta.executionstrategies

import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.executionstrategies.impl.DiscreteEventExecutionImpl
import it.unibo.jakta.executionstrategies.impl.DiscreteTimeExecutionImpl
import it.unibo.jakta.executionstrategies.impl.OneThreadPerAgentImpl
import it.unibo.jakta.executionstrategies.impl.OneThreadPerMasImpl

interface ExecutionStrategy<Belief : Any, Query : Any, Response> {
    fun dispatch(mas: Mas<Belief, Query, Response>, debugEnabled: Boolean)

    fun spawnAgent(agentLC: AgentLifecycle<Belief, Query, Response>)

    fun removeAgent(agentName: String)

    companion object {
        fun <Belief : Any, Query : Any, Response> oneThreadPerAgent(): ExecutionStrategy<Belief, Query, Response> =
            OneThreadPerAgentImpl()

        fun <Belief : Any, Query : Any, Response> oneThreadPerMas(): ExecutionStrategy<Belief, Query, Response> =
            OneThreadPerMasImpl()

        fun <Belief : Any, Query : Any, Response> discreteEventExecution(): ExecutionStrategy<Belief, Query, Response> =
            DiscreteEventExecutionImpl()

        fun <Belief : Any, Query : Any, Response> discreteTimeExecution(): ExecutionStrategy<Belief, Query, Response> =
            DiscreteTimeExecutionImpl()
    }
}
