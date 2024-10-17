package it.unibo.jakta.actions

import it.unibo.jakta.Agent
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.impl.InternalRequestImpl
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Term

interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    val agent: Agent

    companion object {
        fun of(agent: Agent, requestTime: Time?, arguments: Iterable<Term>): InternalRequest =
            InternalRequestImpl(agent, requestTime, arguments.toList())

        fun of(agent: Agent, requestTime: Time?, vararg arguments: Term): InternalRequest =
            of(agent, requestTime, arguments.asList())
    }
}
