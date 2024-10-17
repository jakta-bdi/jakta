package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner

internal class OneThreadPerAgentImpl : ExecutionStrategy {
    private lateinit var executionMas: Mas
    private var debug: Boolean = false
    private val agentsRunners: MutableMap<Agent, Activity.Controller> = mutableMapOf()
    override fun dispatch(mas: Mas, debugEnabled: Boolean) {
        executionMas = mas
        debug = debugEnabled
        mas.agents.forEach { agent ->
            val agentLC = AgentLifecycle.newLifecycleFor(agent)
            Runner.threadOf(
                Activity.of {
                    agentsRunners += agent to it
                    val sideEffects = agentLC.runOneCycle(executionMas.environment, it, debug)
                    executionMas.applyEnvironmentEffects(sideEffects)
                },
            ).run()
        }
    }

    override fun spawnAgent(agent: Agent) {
        val agentLC = AgentLifecycle.newLifecycleFor(agent)
        Runner.threadOf(
            Activity.of {
                agentsRunners += agent to it
                val sideEffects = agentLC.runOneCycle(executionMas.environment, it, debug)
                executionMas.applyEnvironmentEffects(sideEffects)
            },
        ).run()
    }

    override fun removeAgent(agentName: String) {
        val removedAgentController = agentsRunners.filter { it.key.name == agentName }.values.firstOrNull()
        removedAgentController?.stop()
    }
}
