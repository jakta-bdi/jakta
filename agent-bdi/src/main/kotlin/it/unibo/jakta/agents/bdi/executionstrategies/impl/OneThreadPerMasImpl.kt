package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner

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
