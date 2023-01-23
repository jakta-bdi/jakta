package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

interface ExecutionStrategy {
    fun dispatch(agent: Agent, mas: Mas): Runner
}

class SingleThreadedExecutionStrategy() : ExecutionStrategy {
    override fun dispatch(agent: Agent, mas: Mas): Runner {
        val agentLC = AgentLifecycle.of(agent)
        return Runner.threadOf(
            Activity.of {
                val sideEffects = agentLC.reason(mas.environment)
                applySideEffects(sideEffects, mas)
            }
        )
    }

    private fun applySideEffects(effects: Iterable<EnvironmentChange>, mas: Mas) = mas.applyEnvironmentEffects(effects)
}
