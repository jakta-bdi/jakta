package it.unibo.jakta.plans

import it.unibo.jakta.goals.Goal
import it.unibo.jakta.plans.impl.ActivationRecordImpl

// TODO("Sostituire il generico con il tipo corretto")
interface ActivationRecord<Plan> {
    val taskQueue: List<Task> // = plan.tasks

    val plan: Plan

    fun pop(): ActivationRecord<Plan>

    fun applySubstitution(substitution: Substitution): ActivationRecord

    fun isLastGoal(): Boolean

    companion object {
        fun of(goals: List<Goal>, plan: Struct): ActivationRecord =
            ActivationRecordImpl(goals, plan)
    }
}
