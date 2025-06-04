package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import it.unibo.jakta.fsm.time.Time

internal class DiscreteEventExecutionImpl<Belief : Any, Query : Any, Response> : AbstractSingleRunnerExecutionStrategy<Belief, Query, Response>() {

    override fun dispatch(mas: Mas<Belief, Query, Response>, debugEnabled: Boolean) {
        mas.agents.forEach {
            if (!it.hasTimeDistribution) {
                error("ERROR: Can't run a DiscreteEventExecution for agents without a time distribution")
            }
        }
        var time = Time.continuous(0.0)
        mas.agents.forEach {
            synchronizedAgents.addAgent(
                AgentLifecycle.of(it, mas.environment, debugEnabled),
            )
        }
        Runner.simulatedOf(
            Activity.of { controller ->
                // Compute next executions
                mas.agents.forEach { it.controller = controller }
                val timeDistributions = mas.agents.associateWith { it.timeDistribution.invoke(time) }
                val nextEventTime = timeDistributions.values.minOf { it }
                val agentsToExecute = timeDistributions
                    .filter { it.value == nextEventTime }
                    .keys
                    .map { it.context.agentID }

                // Update time
                time = nextEventTime

                // Run Agents
                synchronizedAgents.getAgents()
                    .filter { agentsToExecute.contains(it.agent.context.agentID) }
                    .forEach {
                        val sideEffects = it.runOneCycle()
                        // mas.applyEnvironmentEffects(sideEffects)
                    }
                synchronizedAgents.getAgents().ifEmpty { controller.stop() }
            },
            currentTime = { time },
        ).run()
    }
}
