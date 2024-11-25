package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.plans.ActionTaskEffects
import it.unibo.tuprolog.core.Substitution

interface ActionResponse<C : ActionResult> : ActionTaskEffects<C> {
    val substitution: Substitution
}
