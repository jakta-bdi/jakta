package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import it.unibo.jakta.fsm.time.Time

internal class DiscreteTimeExecutionImpl<Belief : Any, Query : Any, Response>
    : AbstractSingleRunnerExecutionStrategy<Belief, Query, Response>() {
    override fun dispatch(mas: Mas<Belief, Query, Response>, debugEnabled: Boolean) {
        var time = 0
        mas.agents.forEach {
            synchronizedAgents.addAgent(
                AgentLifecycle.of(it, mas.environment, debugEnabled),
            )
        }
        Runner.simulatedOf(
            Activity.of { controller ->
                synchronizedAgents.getAgents().forEach {
                    it.agent.controller = controller
                    val sideEffects = it.runOneCycle()
                    // mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { controller.stop() }
                time++
            },
            currentTime = { Time.discrete(time) },
        ).run()
    }
}
