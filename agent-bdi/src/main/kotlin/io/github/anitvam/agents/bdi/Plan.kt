package io.github.anitvam.agents.bdi

interface Plan {
    val id: PlanID
    val event: Trigger
    val guard: (BeliefBase) -> Boolean
    val goals: List<Goal>
}
