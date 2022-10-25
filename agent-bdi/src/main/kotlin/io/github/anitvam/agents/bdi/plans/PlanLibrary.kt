package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.impl.PlanLibraryImpl

interface PlanLibrary {
    /**
     * Like a standard practice in prolog, plans are ordered to let programmers know when the end of an eventual recursion happens.
     */
    val plans: List<Plan>

    /** @return all the relevant [Plan]s from a given [Event] */
    fun relevantPlans(event : Event) : PlanLibrary

    /** @return all the applicable [Plan]s in the agent with the specified [BeliefBase] */
    fun applicablePlans(beliefBase: BeliefBase) : PlanLibrary

    companion object {
        fun of(plans: List<Plan>) = PlanLibraryImpl(plans)

        fun empty() = PlanLibraryImpl(emptyList())
    }
}
