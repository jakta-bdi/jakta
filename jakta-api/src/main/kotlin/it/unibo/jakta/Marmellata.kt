package it.unibo.jakta

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.Plan

interface Intention<Belief : Any, Query : Any, Result> {
    val stack: List<ActivationRecord<Belief, Query, Result>>
}

interface ActivationRecord<Belief : Any, Query : Any, Result> {
    val origin: Plan<Belief, Query, Result>
    val queue: Sequence<Action<Belief, Query, Result>>
}
