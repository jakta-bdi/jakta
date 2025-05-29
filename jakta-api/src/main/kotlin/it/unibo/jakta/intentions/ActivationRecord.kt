package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.Plan

interface ActivationRecord<Belief : Any, Query : Any, Result> {
    val origin: Plan<Belief, Query, Result>
    val queue: Sequence<Action<Belief, Query, Result>>

    fun isLastActionToExecute(): Boolean = queue.count() == 1

    fun nextActionToExecute(): Action<Belief, Query, Result>? = queue.firstOrNull()

    fun pop(): ActivationRecord<Belief, Query, Result>
}
