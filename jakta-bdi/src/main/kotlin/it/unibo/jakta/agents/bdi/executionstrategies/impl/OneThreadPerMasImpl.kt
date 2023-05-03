package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner

internal class OneThreadPerMasImpl : AbstractSingleRunnerExecutionStrategy() {
    override fun dispatch(mas: Mas) {
        mas.agents.forEach { synchronizedAgents.addAgent(it) }
        Runner.threadOf(
            Activity.of {
                synchronizedAgents.getAgents().forEach { (_, agentLC) ->
                    val sideEffects = agentLC.reason(mas.environment, it)
                    mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { it.stop() }
            }
        ).run()
    }
}
