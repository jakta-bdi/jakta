package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.plans.ActionTaskEffects
import it.unibo.tuprolog.core.Substitution

interface ActionResponse : ActionTaskEffects<ActionSideEffect> {
    val substitution: Substitution
}
