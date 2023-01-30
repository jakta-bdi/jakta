package io.github.anitvam.agents.bdi.executionstrategies.impl

import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

internal class DiscreteTimeExecutionImpl : ExecutionStrategy {
    override fun dispatch(mas: io.github.anitvam.agents.bdi.Mas) {
        var time = 0
        val agentLCs = mas.agents.map { AgentLifecycle.of(it) }
        Runner.threadOf(
            Activity.of {
                agentLCs.forEach { agent ->
                    val sideEffects = agent.reason(mas.environment, it)
                    applySideEffects(sideEffects, mas)
                }
                time++
            }
        ).run()
    }
}
