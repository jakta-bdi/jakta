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
    fun <B : Belief<*>, C : BeliefBase<B, C>> isApplicable(event: Event<T>, beliefBase: C): Boolean

    /** Returns the computed applicable plan */
    fun <B : Belief<*>, C : BeliefBase<B, C>> applicablePlan(event: Event<T>, beliefBase: C): Plan<T, Gu, Go>

    fun isRelevant(event: Event<T>): Boolean

    fun toActivationRecord(): ActivationRecord
}
