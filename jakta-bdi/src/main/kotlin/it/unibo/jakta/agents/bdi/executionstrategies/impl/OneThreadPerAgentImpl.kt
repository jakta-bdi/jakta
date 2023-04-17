package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner

internal class OneThreadPerAgentImpl : ExecutionStrategy {
    override fun dispatch(mas: Mas) {
        mas.agents.forEach { agent ->
            val agentLC = AgentLifecycle.of(agent)
            Runner.threadOf(
                Activity.of {
                    val sideEffects = agentLC.reason(mas.environment, it)
                    applySideEffects(sideEffects, mas)
                }
            ).run()
        }
    }
}
