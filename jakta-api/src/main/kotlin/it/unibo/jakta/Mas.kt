package it.unibo.jakta

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.executionstrategies.ExecutionStrategy
import it.unibo.jakta.impl.MasImpl
import kotlin.collections.plus

interface Mas<Belief : Any, Query : Any, Response> {
    val environment: AgentProcess<Belief>
    val agents: Iterable<Agent<Belief, Query, Response>>
    val executionStrategy: ExecutionStrategy<Belief, Query, Response>

    fun start(debugEnabled: Boolean = false)

    fun applyEnvironmentEffects(effects: Iterable<EnvironmentChange<Belief, Query, Response>>)

    companion object {
        fun <Belief : Any, Query : Any, Response> of(
            executionStrategy: ExecutionStrategy<Belief, Query, Response>,
            environment: AgentProcess<Belief>,
            agent: Agent<Belief, Query, Response>,
            vararg agents: Agent<Belief, Query, Response>,
        ): Mas<Belief, Query, Response> = of(executionStrategy, environment, agents.asIterable() + agent)

        fun <Belief : Any, Query : Any, Response> of(
            executionStrategy: ExecutionStrategy<Belief, Query, Response>,
            environment: AgentProcess<Belief>,
            agents: Iterable<Agent<Belief, Query, Response>>,
        ): Mas<Belief, Query, Response> = MasImpl(executionStrategy, environment, agents)
    }
}
