package it.unibo.jakta.agents.bdi.executionstrategies.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.Mas
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionStrategy
import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.Runner

internal class OneThreadPerAgentImpl : ExecutionStrategy {
    private lateinit var executionMas: Mas
    private val agentsRunners: MutableMap<Agent, Activity.Controller> = mutableMapOf()

    override fun dispatch(mas: Mas) {
        executionMas = mas
        mas.agents.forEach { agent ->
            val agentLC = AgentLifecycle.of(agent)
            Runner.threadOf(
                Activity.of {
                    agentsRunners += agent to it
                    val sideEffects = agentLC.reason(executionMas.environment, it)
                    executionMas.applyEnvironmentEffects(sideEffects)
                }
            ).run()
        }
    }

    override fun spawnAgent(agent: Agent) {
        val agentLC = AgentLifecycle.of(agent)
        Runner.threadOf(
            Activity.of {
                agentsRunners += agent to it
                val sideEffects = agentLC.reason(executionMas.environment, it)
                executionMas.applyEnvironmentEffects(sideEffects)
            }
        ).run()
    }

    override fun removeAgent(agentName: String) {
        val removedAgentController = agentsRunners.filter { it.key.name == agentName }.values.firstOrNull()
        removedAgentController?.stop()
    }
}
