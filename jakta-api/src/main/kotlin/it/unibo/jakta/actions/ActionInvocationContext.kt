package it.unibo.jakta.actions

import it.unibo.jakta.Agent
import it.unibo.jakta.fsm.time.Time
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface ActionInvocationContext<Belief : Any, Query : Any, Result> {
    val agentContext: Agent.Context<Belief, Query, Result>
    @OptIn(ExperimentalTime::class)
    val invocationTimestamp: Instant
}
