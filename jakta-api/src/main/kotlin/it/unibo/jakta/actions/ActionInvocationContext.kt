package it.unibo.jakta.actions

import it.unibo.jakta.Agent
import it.unibo.jakta.fsm.time.Time

interface ActionInvocationContext<Belief : Any, Query : Any, Result> {
    val agentContext: Agent.Context<Belief, Query, Result>
    val invocationTimestamp: Time?
}
