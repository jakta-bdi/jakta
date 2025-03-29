package it.unibo.jakta.agents.bdi.plans.impl

import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary

internal data class PlanLibraryImpl(
    override val plans: List<Plan>,
) : PlanLibrary {
    override fun relevantPlans(event: Event): PlanLibrary = PlanLibrary.of(plans.filter { it.isRelevant(event) })

    override fun applicablePlans(
        event: Event,
        beliefBase: BeliefBase,
    ): PlanLibrary = PlanLibrary.of(plans.filter { it.isApplicable(event, beliefBase) })

    override fun addPlan(plan: Plan): PlanLibrary = PlanLibrary.of(plans + plan)

    override fun removePlan(plan: Plan): PlanLibrary = PlanLibrary.of(plans - plan)
}
