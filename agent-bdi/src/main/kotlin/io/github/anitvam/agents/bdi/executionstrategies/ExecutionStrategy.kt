package io.github.anitvam.agents.bdi.executionstrategies

import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.executionstrategies.impl.OneThreadPerAgentImpl
import io.github.anitvam.agents.bdi.executionstrategies.impl.OneThreadPerMasImpl
import io.github.anitvam.agents.bdi.executionstrategies.impl.DiscreteEventExecutionImpl
import io.github.anitvam.agents.bdi.executionstrategies.impl.DiscreteTimeExecutionImpl

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
