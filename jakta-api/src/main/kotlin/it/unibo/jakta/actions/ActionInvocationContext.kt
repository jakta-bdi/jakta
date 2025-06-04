package it.unibo.jakta.actions

import it.unibo.jakta.Agent
import it.unibo.jakta.fsm.time.Time
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface ActionInvocationContext<Belief : Any, Query : Any, Result> {
    val agentContext: Agent.Context<Belief, Query, Result>
    @OptIn(ExperimentalTime::class)
    val invocationTimestamp: Instant

    companion object {
        @OptIn(ExperimentalTime::class)
        operator fun <Belief : Any, Query : Any, Result> invoke(
            agentContext: Agent.Context<Belief, Query, Result>,
            invocationTimestamp: Instant,
        ) = object : ActionInvocationContext<Belief, Query, Result> {
            override val agentContext: Agent.Context<Belief, Query, Result>
                get() = agentContext
            override val invocationTimestamp: Instant
                get() = invocationTimestamp

        }
    }
}
