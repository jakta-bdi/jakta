package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.tuprolog.core.Substitution

class ExternalResponse(
    override val substitution: Substitution,
    effects: Iterable<EnvironmentChange>,
) : ActionResponse<EnvironmentChange>, List<EnvironmentChange> by effects.toList()
