package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.impl.InternalRequestImpl
import io.github.anitvam.agents.fsm.time.Time
import it.unibo.tuprolog.core.Term

interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    val agent: Agent

    companion object {
        fun of(agent: Agent, requestTime: Time, arguments: Iterable<Term>): InternalRequest =
            InternalRequestImpl(agent, requestTime, arguments.toList())

        fun of(agent: Agent, requestTime: Time, vararg arguments: Term): InternalRequest =
            of(agent, requestTime, arguments.asList())
    }
}
