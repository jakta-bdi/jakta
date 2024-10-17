package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.Mas
import it.unibo.jakta.executionstrategies.hasTimeDistribution
import it.unibo.jakta.executionstrategies.timeDistribution
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import it.unibo.jakta.fsm.time.Time

internal class DiscreteEventExecutionImpl : AbstractSingleRunnerExecutionStrategy() {

    override fun dispatch(mas: Mas, debugEnabled: Boolean) {
        mas.agents.forEach {
            if (!it.hasTimeDistribution) {
                error("ERROR: Can't run a DiscreteEventExecution for agents without a time distribution")
            }
        }
        var time = Time.continuous(0.0)
        mas.agents.forEach { synchronizedAgents.addAgent(it) }
        Runner.simulatedOf(
            Activity.of {
                // Compute next executions
                val timeDistributions = mas.agents.associateWith { it.timeDistribution.invoke(time) }
                val nextEventTime = timeDistributions.values.minOf { it }
                val agentsToExecute = timeDistributions.filter { it.value == nextEventTime }.keys.map { it.agentID }

                // Update time
                time = nextEventTime

                // Run Agents
                synchronizedAgents.getAgents()
                    .filter { (agent, _) -> agentsToExecute.contains(agent.agentID) }
                    .forEach { (_, agentLC) ->
                        val sideEffects = agentLC.runOneCycle(mas.environment, it, debugEnabled)
                        mas.applyEnvironmentEffects(sideEffects)
                    }
                synchronizedAgents.getAgents().ifEmpty { it.stop() }
            },
            currentTime = { time },
        ).run()
    }
}
