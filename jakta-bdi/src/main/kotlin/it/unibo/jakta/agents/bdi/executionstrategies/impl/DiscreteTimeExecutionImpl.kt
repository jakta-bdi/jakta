package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner
import it.unibo.jakta.agents.fsm.time.Time

internal class DiscreteTimeExecutionImpl : AbstractSingleRunnerExecutionStrategy() {
    override fun dispatch(mas: it.unibo.jakta.agents.bdi.Mas) {
        var time = 0
        mas.agents.forEach { synchronizedAgents.addAgent(it) }
        Runner.simulatedOf(
            Activity.of {
                synchronizedAgents.getAgents().forEach { (_, agentLC) ->
                    val sideEffects = agentLC.reason(mas.environment, it)
                    mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { it.stop() }
                time++
            },
            currentTime = { Time.discrete(time) }
        ).run()
    }
}
