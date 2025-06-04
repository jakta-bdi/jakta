package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner

internal class OneThreadPerMasImpl<Belief : Any, Query : Any, Response>
    : AbstractSingleRunnerExecutionStrategy<Belief, Query, Response>() {
    override fun dispatch(mas: Mas<Belief, Query, Response>, debugEnabled: Boolean) {
        mas.agents.forEach {
            synchronizedAgents.addAgent(
                AgentLifecycle.of(
                    it,
                    mas.environment,
                    debugEnabled,
                ),
            )
        }
        Runner.threadOf(
            Activity.of { controller ->
                synchronizedAgents.getAgents().forEach {
                    it.agent.controller = controller
                    val sideEffects = it.runOneCycle()
                    // mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { controller.stop() }
            },
        ).run()
    }
}
