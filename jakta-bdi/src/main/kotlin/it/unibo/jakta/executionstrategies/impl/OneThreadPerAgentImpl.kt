package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.ASAgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner

internal class OneThreadPerAgentImpl : ExecutionStrategy {
    private lateinit var executionMas: Mas
    private var debug: Boolean = false
    private val runningAgents = mutableListOf<ASAgent>()
    override fun dispatch(mas: Mas, debugEnabled: Boolean) {
        executionMas = mas
        debug = debugEnabled
        mas.agents.forEach { agent ->
            val agentLC = ASAgentLifecycle.of(agent, mas.environment, debugEnabled)
            Runner.threadOf(
                Activity.of {
                    agent.controller = it
                    runningAgents += agent
                    val sideEffects = agentLC.runOneCycle()
                    // executionMas.applyEnvironmentEffects(sideEffects)
                },
            ).run()
        }
    }

    override fun spawnAgent(agentLC: ASAgentLifecycle) {
        Runner.threadOf(
            Activity.of {
                val sideEffects = agentLC.runOneCycle()
                // executionMas.applyEnvironmentEffects(sideEffects)
            },
        ).run()
    }

    override fun removeAgent(agentName: String) {
        val removedAgentController = runningAgents.firstOrNull { it.context.agentName == agentName }?.controller
        removedAgentController?.stop()
    }
}
