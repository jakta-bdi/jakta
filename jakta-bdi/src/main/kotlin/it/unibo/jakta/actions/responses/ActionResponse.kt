package it.unibo.jakta.actions.responses

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.plans.ActionTaskEffects
import it.unibo.tuprolog.core.Substitution

class ActionResponse(
    val substitution: Substitution,
    effects: List<ActionSideEffect>,
) : ActionTaskEffects<ActionSideEffect>, List<ActionSideEffect> by effects
