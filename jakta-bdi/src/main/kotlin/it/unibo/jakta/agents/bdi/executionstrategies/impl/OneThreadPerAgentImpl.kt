package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner

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
