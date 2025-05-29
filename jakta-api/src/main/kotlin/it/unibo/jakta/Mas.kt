package it.unibo.jakta

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.impl.MasImpl
import kotlin.collections.plus

interface Mas<Belief : Any, Query : Any, Response> {
    val environment: AgentProcess
    val agents: Iterable<Agent<Belief, Query, Response>>
    val executionStrategy: ExecutionStrategy<Belief, Query, Response>

    fun start(debugEnabled: Boolean = false)

    fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange>)

    companion object {
        fun <Belief : Any, Query : Any, Response> of(
            executionStrategy: ExecutionStrategy<Belief, Query, Response>,
            environment: AgentProcess,
            agent: Agent<Belief, Query, Response>,
            vararg agents: Agent<Belief, Query, Response>,
        ): Mas<Belief, Query, Response> = of(executionStrategy, environment, agents.asIterable() + agent)

        fun <Belief : Any, Query : Any, Response> of(
            executionStrategy: ExecutionStrategy<Belief, Query, Response>,
            environment: AgentProcess,
            agents: Iterable<Agent<Belief, Query, Response>>,
        ): Mas<Belief, Query, Response> = MasImpl(executionStrategy, environment, agents)
    }
}
