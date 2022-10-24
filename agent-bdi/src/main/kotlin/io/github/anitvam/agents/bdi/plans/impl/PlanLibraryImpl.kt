package io.github.anitvam.agents.bdi.plans.impl

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

class PlanLibraryImpl(override val plans: List<Plan>) : PlanLibrary {
    override fun relevantPlans(event: Event): PlanLibrary {
        var relevantPlans : Set<Plan> = emptySet()
        plans.forEach {
            if((it.event.value mguWith event.trigger.value).isSuccess) {
                relevantPlans = relevantPlans + it
            }
        }
        return PlanLibrary.of(relevantPlans)
    }

    override fun applicablePlans(beliefBase: BeliefBase) : PlanLibrary = TODO()
}