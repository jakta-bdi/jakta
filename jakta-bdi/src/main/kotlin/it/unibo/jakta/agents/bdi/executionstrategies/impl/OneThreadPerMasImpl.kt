package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner

internal class OneThreadPerMasImpl : AbstractSingleRunnerExecutionStrategy() {
    override fun dispatch(
        mas: Mas,
        debugEnabled: Boolean,
    ) {
        mas.agents.forEach { synchronizedAgents.addAgent(it) }
        Runner
            .threadOf(
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
