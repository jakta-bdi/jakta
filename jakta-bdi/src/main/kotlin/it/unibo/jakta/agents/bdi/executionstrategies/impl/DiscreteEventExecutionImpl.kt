package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.bdi.executionstrategies.hasTimeDistribution
import it.unibo.jakta.agents.bdi.executionstrategies.timeDistribution
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner
import it.unibo.jakta.agents.fsm.time.Time

internal class DiscreteEventExecutionImpl : ExecutionStrategy {
    override fun dispatch(mas: Mas) {
        mas.agents.forEach {
            if (!it.hasTimeDistribution) {
                error("ERROR: Can't run a DiscreteEventExecution for agents without a time distribution")
            }
        }
        var time = Time.continuous(0.0)
        val agentLCs = mas.agents.associate { it.agentID to AgentLifecycle.of(it) }
        Runner.simulatedOf(
            Activity.of {
                // Compute next executions
                val timeDistributions = mas.agents.associateWith { it.timeDistribution.invoke(time) }
                val nextEventTime = timeDistributions.values.minOf { it }
                val agentsToExecute = timeDistributions.filter { it.value == nextEventTime }.keys.map { it.agentID }

                // Update time
                time = nextEventTime

                // Run Agents
                agentLCs
                    .filter { (agent, _) -> agentsToExecute.contains(agent) }
                    .forEach { (_, agentLC) ->
                        val sideEffects = agentLC.reason(mas.environment, it)
                        applySideEffects(sideEffects, mas)
                    }
            },
            currentTime = { time }
        ).run()
    }
}
