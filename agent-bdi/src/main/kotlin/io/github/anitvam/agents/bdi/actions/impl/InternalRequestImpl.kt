package io.github.anitvam.agents.bdi.actions.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.actions.InternalResponse
import io.github.anitvam.agents.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

data class InternalRequestImpl(
    override val agent: Agent,
    override val arguments: List<Term>,
    override val requestTimestamp: Time = Time.of(System.currentTimeMillis()),
) : InternalRequest {
    override fun reply(substitution: Substitution, effects: Iterable<AgentChange>) =
        InternalResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: AgentChange) =
        reply(substitution, effects.asList())
}
