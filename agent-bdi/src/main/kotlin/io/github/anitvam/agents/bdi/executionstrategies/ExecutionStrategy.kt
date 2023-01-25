package io.github.anitvam.agents.bdi.executionstrategies

import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

interface ExecutionStrategy {
    fun dispatch(mas: Mas)

    fun applySideEffects(effects: Iterable<EnvironmentChange>, mas: Mas) = mas.applyEnvironmentEffects(effects)
}

class OneThreadPerAgent : ExecutionStrategy {
    override fun dispatch(mas: Mas) {
        mas.agents.forEach {
            val agentLC = AgentLifecycle.of(it)
            Runner.threadOf(
                Activity.of {
                    val sideEffects = agentLC.reason(mas.environment)
                    applySideEffects(sideEffects, mas)
                }
            ).run()
        }
    }
}

class OneThreadPerMas : ExecutionStrategy {
    override fun dispatch(mas: Mas) {
        val agentLCs = mas.agents.map { AgentLifecycle.of(it) }
        Runner.threadOf(
            Activity.of {
                agentLCs.forEach {
                    val sideEffects = it.reason(mas.environment)
                    applySideEffects(sideEffects, mas)
                }
            }
        ).run()
    }
}
