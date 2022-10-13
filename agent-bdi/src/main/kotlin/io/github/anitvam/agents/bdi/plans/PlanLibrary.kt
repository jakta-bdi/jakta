package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event

interface PlanLibrary {
    val plans: Set<Plan>

    /** @return all the relevant [Plan]s from a given [Event] */
    fun relevantPlans(event : Event) : PlanLibrary

    /** @return all the applicable [Plan]s in the agent with the specified [BeliefBase] */
    fun applicablePlans(beliefBase: BeliefBase) : PlanLibrary

    companion object {
        fun of(plans: Set<Plan>) = PlanLibraryImpl(plans)
    }
}
