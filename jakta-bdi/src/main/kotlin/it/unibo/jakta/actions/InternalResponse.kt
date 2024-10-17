package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.tuprolog.core.Substitution

data class InternalResponse(
    override val substitution: Substitution,
    override val effects: Iterable<AgentChange>,
) : ActionResponse<AgentChange>
