package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.impl.ActivationRecordImpl
import it.unibo.tuprolog.core.Substitution

interface ActivationRecord {
    val goalQueue: List<Goal>

    val plan: PlanID

    fun pop(): ActivationRecord

    fun applySubstitution(substitution: Substitution): ActivationRecord

    companion object {
        fun of(goals: List<Goal>): ActivationRecord = ActivationRecordImpl(goals)
    }
}
