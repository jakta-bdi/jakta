package it.unibo.jakta.executionstrategies.impl

import it.unibo.jakta.Agent
import it.unibo.jakta.AgentLifecycle
import it.unibo.jakta.Mas
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import kotlin.collections.plusAssign

internal class OneThreadPerAgentImpl<Belief : Any, Query : Any, Response> : ExecutionStrategy<Belief, Query, Response> {
    private lateinit var executionMas: Mas<Belief, Query, Response>
    private var debug: Boolean = false
    private val runningAgents = mutableListOf<Agent<Belief, Query, Response>>()
    override fun dispatch(mas: Mas<Belief, Query, Response>, debugEnabled: Boolean) {
        executionMas = mas
        debug = debugEnabled
        mas.agents.forEach { agent ->
            val agentLC = AgentLifecycle.of(agent, mas.environment, debugEnabled)
            Runner.threadOf(
                Activity.of {
                    agent.controller = it
                    runningAgents plusAssign agent
                    val sideEffects = agentLC.runOneCycle()
                    // executionMas.applyEnvironmentEffects(sideEffects)
                },
            ).run()
        }
    }

    override fun spawnAgent(agentLC: AgentLifecycle<Belief, Query, Response>) {
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
