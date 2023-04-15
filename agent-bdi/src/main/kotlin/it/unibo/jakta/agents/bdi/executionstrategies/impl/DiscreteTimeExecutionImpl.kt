package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner
import it.unibo.jakta.agents.fsm.time.Time

internal class DiscreteTimeExecutionImpl : ExecutionStrategy {
    override fun dispatch(mas: it.unibo.jakta.agents.bdi.Mas) {
        var time = 0
        val agentLCs = mas.agents.map { AgentLifecycle.of(it) }
        Runner.simulatedOf(
            Activity.of {
                agentLCs.forEach { agent ->
                    val sideEffects = agent.reason(mas.environment, it)
                    applySideEffects(sideEffects, mas)
                }
                time++
            },
            currentTime = { Time.discrete(time) }
        ).run()
    }
}
