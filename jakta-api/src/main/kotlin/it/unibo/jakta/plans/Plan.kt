package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.Goal

interface Plan<T : Trigger<*>, Gu : Guard<*>, Go : Goal<*>> {
    val trigger: T
    val guard: Gu
    val goals: List<Go>

    /** Determines if a plan is applicable */
    fun <Query, Belief, BB : BeliefBase<Query, Belief, BB>> isApplicable(event: Event<T>, beliefBase: BB): Boolean

    /** Returns the computed applicable plan */
    fun <B, C : BeliefBase<B, C>> applicablePlan(event: Event<T>, beliefBase: C): Plan<T, Gu, Go>

    fun isRelevant(event: Event<T>): Boolean

    fun toActivationRecord(): ActivationRecord
}
