package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.tuprolog.core.Substitution

class InternalResponse(
    override val substitution: Substitution,
    effects: Iterable<AgentChange>,
) : ActionResponse<AgentChange>, List<AgentChange> by effects.toList()
