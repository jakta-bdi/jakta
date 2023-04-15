package it.unibo.jakta.agents.bdi.executionstrategies

import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.executionstrategies.impl.OneThreadPerAgentImpl
import it.unibo.jakta.agents.bdi.executionstrategies.impl.OneThreadPerMasImpl
import it.unibo.jakta.agents.bdi.executionstrategies.impl.DiscreteEventExecutionImpl
import it.unibo.jakta.agents.bdi.executionstrategies.impl.DiscreteTimeExecutionImpl

interface ExecutionStrategy {
    fun dispatch(mas: Mas)

    fun applySideEffects(effects: Iterable<EnvironmentChange>, mas: Mas) = mas.applyEnvironmentEffects(effects)

    companion object {
        fun oneThreadPerAgent(): ExecutionStrategy = OneThreadPerAgentImpl()

        fun oneThreadPerMas(): ExecutionStrategy = OneThreadPerMasImpl()

        fun discreteEventExecution(): ExecutionStrategy = DiscreteEventExecutionImpl()

        fun discreteTimeExecution(): ExecutionStrategy = DiscreteTimeExecutionImpl()
    }
}
