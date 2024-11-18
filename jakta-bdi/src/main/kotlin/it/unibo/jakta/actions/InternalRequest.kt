package it.unibo.jakta.actions

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.impl.InternalRequestImpl
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Term

interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    val agent: ASAgent // TODO("Questo potrebbe diventare un AgentContext")

    companion object {
        fun of(agent: ASAgent, requestTime: Time?, arguments: Iterable<Term>): InternalRequest =
            InternalRequestImpl(agent, requestTime, arguments.toList())

        fun of(agent: ASAgent, requestTime: Time?, vararg arguments: Term): InternalRequest =
            of(agent, requestTime, arguments.asList())
    }
}
