package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.actions.impl.InternalRequestImpl
import it.unibo.jakta.agents.fsm.time.Time
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
