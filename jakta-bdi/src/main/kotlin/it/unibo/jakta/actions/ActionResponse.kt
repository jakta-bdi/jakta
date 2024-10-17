package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution

interface ActionResponse<C : SideEffect> {
    val substitution: Substitution
    val effects: Iterable<C>
}
