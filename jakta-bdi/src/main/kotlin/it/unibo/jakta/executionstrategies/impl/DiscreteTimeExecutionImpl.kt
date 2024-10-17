package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.Mas
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import it.unibo.jakta.fsm.time.Time

internal class DiscreteTimeExecutionImpl : AbstractSingleRunnerExecutionStrategy() {
    override fun dispatch(mas: Mas, debugEnabled: Boolean) {
        var time = 0
        mas.agents.forEach { synchronizedAgents.addAgent(it) }
        Runner.simulatedOf(
            Activity.of {
                synchronizedAgents.getAgents().forEach { (_, agentLC) ->
                    val sideEffects = agentLC.runOneCycle(mas.environment, it, debugEnabled)
                    mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { it.stop() }
                time++
            },
            currentTime = { Time.discrete(time) },
        ).run()
    }
}
