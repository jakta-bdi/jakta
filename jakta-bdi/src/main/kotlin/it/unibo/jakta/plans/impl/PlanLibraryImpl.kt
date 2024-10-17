package it.unibo.jakta.plans.impl

import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PlanLibrary

internal data class PlanLibraryImpl(override val plans: List<Plan>) : PlanLibrary {
    override fun relevantPlans(event: Event): PlanLibrary =
        PlanLibrary.of(plans.filter { it.isRelevant(event) })

    override fun applicablePlans(event: Event, beliefBase: PrologBeliefBase): PlanLibrary =
        PlanLibrary.of(plans.filter { it.isApplicable(event, beliefBase) })

    override fun addPlan(plan: Plan): PlanLibrary = PlanLibrary.of(plans + plan)

    override fun removePlan(plan: Plan): PlanLibrary = PlanLibrary.of(plans - plan)
}
