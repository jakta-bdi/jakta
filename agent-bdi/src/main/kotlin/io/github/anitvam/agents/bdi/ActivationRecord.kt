package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.PlanID

interface ActivationRecord {
    val goalQueue: List<Goal>

    val plan: PlanID

    fun pop(): ActivationRecord
}
