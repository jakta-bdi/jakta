package io.github.anitvam.agents.bdi.plans.impl

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

internal class PlanLibraryImpl(override val plans: List<Plan>) : PlanLibrary {
    override fun relevantPlans(event: Event): PlanLibrary =
        PlanLibrary.of(plans.filter { it.isRelevant(event) })

    override fun applicablePlans(event: Event, beliefBase: BeliefBase): PlanLibrary =
        PlanLibrary.of(plans.filter { it.isApplicable(event, beliefBase) })
}
