package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.tuprolog.core.Substitution

data class InternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<AgentChange>,
) : ActionResponse<AgentChange>
