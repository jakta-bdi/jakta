package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.Plan

interface ActivationRecord<Belief : Any, Query : Any, Result> {
    val origin: Plan<Belief, Query, Result>
    val queue: Sequence<Action<Belief, Query, Result>>
}