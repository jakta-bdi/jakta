package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.Goal

interface Plan {
    val id: PlanID
    val event: Trigger
    val guard: (BeliefBase) -> Boolean
    val goals: List<Goal>
}
