package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.SideEffect
import it.unibo.jakta.fsm.time.Time
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

interface ActionRequest<C : SideEffect, Res : ActionResponse<C>> {
    val arguments: List<Term>
    val requestTimestamp: Time?

    fun reply(substitution: Substitution = Substitution.empty(), effects: Iterable<C>): Res

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: C): Res
}
