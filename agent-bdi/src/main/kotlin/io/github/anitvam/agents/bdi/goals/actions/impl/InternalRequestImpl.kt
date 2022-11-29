package io.github.anitvam.agents.bdi.goals.actions.impl

import io.github.anitvam.agents.bdi.goals.actions.AgentChange
import io.github.anitvam.agents.bdi.goals.actions.InternalRequest
import io.github.anitvam.agents.bdi.goals.actions.InternalResponse
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

data class InternalRequestImpl(
    override val arguments: List<Term>,
) : InternalRequest {
    override fun reply(substitution: Substitution, effects: Iterable<AgentChange>) =
        InternalResponse(substitution, effects)

    override fun reply(substitution: Substitution, vararg effects: AgentChange) =
        reply(substitution, effects.asList())
}
