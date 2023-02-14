package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import it.unibo.tuprolog.core.Substitution

data class InternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<AgentChange>,
) : ActionResponse<AgentChange>
