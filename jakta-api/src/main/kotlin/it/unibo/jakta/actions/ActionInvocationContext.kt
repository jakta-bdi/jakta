package it.unibo.jakta.actions

import it.unibo.jakta.Agent
import it.unibo.jakta.fsm.time.Time

interface ActionInvocationContext {
    val agent: Agent<*>
    val invocationTimestamp: Time?
}
