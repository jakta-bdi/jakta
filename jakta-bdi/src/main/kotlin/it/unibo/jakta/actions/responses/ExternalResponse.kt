package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.tuprolog.core.Substitution

class ExternalResponse(
    override val substitution: Substitution,
    effects: Iterable<ActionSideEffect<Any>>,
) : ActionResponse<Any, ActionSideEffect<Any>>, List<ActionSideEffect<Any>> by effects.toList()
