package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.tuprolog.core.Substitution

class ExternalResponse(
    override val substitution: Substitution,
    effects: Iterable<ActionResult<Any>>,
) : ActionResponse<Any, ActionResult<Any>>, List<ActionResult<Any>> by effects.toList()
