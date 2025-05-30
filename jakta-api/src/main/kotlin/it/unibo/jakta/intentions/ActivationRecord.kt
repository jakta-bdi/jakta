package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.Plan

interface ActivationRecord<Belief : Any, Query : Any, Result> {
    val origin: Plan<Belief, Query, Result>
//    val queue: Sequence<Action<Belief, Query, Result>>

    fun isEmpty(): Boolean

    fun pop(): Popped<Belief, Query, Result>

    data class Popped<Belief : Any, Query : Any, Result>(
        val activationRecord: ActivationRecord<Belief, Query, Result>,
        val action: Action<Belief, Query, Result>,
    )
}
