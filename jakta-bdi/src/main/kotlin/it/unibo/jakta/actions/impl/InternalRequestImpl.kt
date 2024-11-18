package it.unibo.jakta.actions.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

data class InternalRequestImpl(
    override val agent: ASAgent,
    override val requestTimestamp: Time?,
    override val arguments: List<Term>,
) : InternalRequest {
    override fun reply(substitution: Substitution, effects: Iterable<AgentChange>) =
        InternalResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: AgentChange) =
        reply(substitution, effects.asList())
}
