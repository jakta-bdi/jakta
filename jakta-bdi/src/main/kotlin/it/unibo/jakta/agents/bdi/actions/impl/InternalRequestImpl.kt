package it.unibo.jakta.agents.bdi.actions.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.InternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.jakta.agents.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

data class InternalRequestImpl(
    override val agent: Agent,
    override val requestTimestamp: Time?,
    override val arguments: List<Term>,
) : InternalRequest {
    override fun reply(substitution: Substitution, effects: Iterable<AgentChange>) =
        InternalResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: AgentChange) =
        reply(substitution, effects.asList())
}
