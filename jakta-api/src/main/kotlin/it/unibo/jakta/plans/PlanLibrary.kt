package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.plans.impl.PlanLibraryImpl

interface PlanLibrary<T, G, P> where
      T : Trigger<*>,
      G : Guard<*>,
      P : Plan<T, G> {
    /**
     * Like a standard practice in prolog, plans are ordered to let programmers know when the end of an eventual recursion happens.
     */
    val plans: List<P>

    /** @return all the relevant [Plan]s from a given [Event] */
    fun relevantPlans(event: Event<T>): PlanLibrary<T, G, P>

    /** @return all the applicable [Plan]s in the agent with the specified [PrologBeliefBase] */
    fun applicablePlans(event: Event<T>, beliefBase: PrologBeliefBase): PlanLibrary

    fun addPlan(plan: Plan): PlanLibrary

    fun removePlan(plan: Plan): PlanLibrary

    companion object {
        fun of(plans: List<Plan>): PlanLibrary = PlanLibraryImpl(plans)
        fun of(vararg plans: Plan): PlanLibrary = of(plans.asList())

        fun empty(): PlanLibrary = PlanLibraryImpl(emptyList())
    }
}
