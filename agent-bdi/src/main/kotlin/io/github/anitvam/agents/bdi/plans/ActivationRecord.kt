package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.goals.Goal

interface ActivationRecord {
    val goalQueue: List<Goal>

    val plan: PlanID

    fun pop(): ActivationRecord
}
