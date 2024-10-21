package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.Goal

interface PlanLibrary<T, Gu, Go, P> where
      T : Trigger<*>,
      Gu : Guard<*>,
      Go : Goal<*>,
      P : Plan<T, Gu, Go> {
    /**
     * Like a standard practice in prolog, plans are ordered to let programmers know when the end of an eventual recursion happens.
     */
    val plans: List<P>

    /** @return all the relevant [Plan]s from a given [Event] */
    fun relevantPlans(event: Event<T>): PlanLibrary<T, Gu, Go, P>

    /** @return all the applicable [Plan]s in the agent with the specified [BeliefBase] */
    fun <B : Belief<*>, BB : BeliefBase<B, BB>> applicablePlans(
        event: Event<T>,
        beliefBase: BB,
    ): PlanLibrary<T, Gu, Go, P>

    fun addPlan(plan: P): PlanLibrary<T, Gu, Go, P>

    fun removePlan(plan: P): PlanLibrary<T, Gu, Go, P>
}
