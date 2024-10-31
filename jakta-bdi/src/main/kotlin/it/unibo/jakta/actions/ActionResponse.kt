package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.tuprolog.core.Substitution

interface ActionResponse<C : ActionResult> {
    val substitution: Substitution
    val effects: Iterable<C>
}
