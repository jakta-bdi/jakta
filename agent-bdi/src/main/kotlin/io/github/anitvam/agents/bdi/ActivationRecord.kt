package io.github.anitvam.agents.bdi

interface ActivationRecord {
    val goalQueue: List<Goal>

    val plan: PlanID

    fun pop(): ActivationRecord
}
