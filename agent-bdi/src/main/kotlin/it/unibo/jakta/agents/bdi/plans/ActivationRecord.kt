package it.unibo.jakta.agents.bdi.plans

import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.jakta.agents.bdi.plans.impl.ActivationRecordImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

interface ActivationRecord {
    val goalQueue: List<Goal>

    val plan: Struct

    fun pop(): ActivationRecord

    fun applySubstitution(substitution: Substitution): ActivationRecord

    fun isLastGoal(): Boolean

    companion object {
        fun of(goals: List<Goal>, plan: Struct): ActivationRecord = ActivationRecordImpl(goals, plan)
    }
}
