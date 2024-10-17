package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.Mas
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner

internal class OneThreadPerMasImpl : AbstractSingleRunnerExecutionStrategy() {
    override fun dispatch(mas: Mas, debugEnabled: Boolean) {
        mas.agents.forEach { synchronizedAgents.addAgent(it) }
        Runner.threadOf(
            Activity.of {
                synchronizedAgents.getAgents().forEach { (_, agentLC) ->
                    val sideEffects = agentLC.runOneCycle(mas.environment, it, debugEnabled)
                    mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { it.stop() }
            },
        ).run()
    }
}
