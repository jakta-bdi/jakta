package io.github.anitvam.agents.bdi.executionstrategies.impl

import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.Mas
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Runner

internal class OneThreadPerMasImpl : ExecutionStrategy {
    override fun dispatch(mas: Mas) {
        val agentLCs = mas.agents.map { AgentLifecycle.of(it) }
        Runner.threadOf(
            Activity.of {
                agentLCs.forEach { agent ->
                    val sideEffects = agent.reason(mas.environment, it)
                    applySideEffects(sideEffects, mas)
                }
            }
        ).run()
    }
}
