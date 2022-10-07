package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Trigger

interface Plan {
    val id: PlanID
    val event: Trigger
    val guard: (BeliefBase) -> Boolean
    val goals: List<Goal>
}
