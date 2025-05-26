package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.ASAgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import it.unibo.jakta.fsm.time.Time

internal class DiscreteTimeExecutionImpl : AbstractSingleRunnerExecutionStrategy() {
    override fun dispatch(mas: Mas, debugEnabled: Boolean) {
        var time = 0
        mas.agents.forEach {
            synchronizedAgents.addAgent(
                ASAgentLifecycle.of(it, mas.environment, debugEnabled),
            )
        }
        Runner.simulatedOf(
            Activity.of {
                synchronizedAgents.getAgents().forEach {
                    val sideEffects = it.runOneCycle()
                    // mas.applyEnvironmentEffects(sideEffects)
                }
                synchronizedAgents.getAgents().ifEmpty { it.stop() }
                time++
            },
            currentTime = { Time.discrete(time) },
        ).run()
    }
}
