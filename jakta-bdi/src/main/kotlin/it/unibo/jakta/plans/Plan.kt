package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.TestGoalFailure
import it.unibo.jakta.events.TestGoalInvocation
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.Goal
import it.unibo.jakta.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth

interface Plan {
    val trigger: Trigger
    val guard: Struct
    val goals: List<Goal>

    /** Determines if a plan is applicable */
    fun isApplicable(event: Event, beliefBase: PrologBeliefBase): Boolean

    /** Returns the computed applicable plan */
    fun applicablePlan(event: Event, beliefBase: PrologBeliefBase): Plan

    fun isRelevant(event: Event): Boolean

    fun toActivationRecord(): ActivationRecord

    companion object {
        private fun of(trigger: Trigger, guard: Struct, goals: List<Goal>): Plan =
            PlanImpl(trigger, guard, goals)

        fun ofBeliefBaseAddition(
            belief: Belief,
            goals: List<Goal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(BeliefBaseAddition(belief), guard, goals)

        fun ofBeliefBaseRemoval(
            belief: Belief,
            goals: List<Goal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(BeliefBaseRemoval(belief), guard, goals)

        fun ofAchievementGoalInvocation(
            value: Struct,
            goals: List<Goal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(AchievementGoalInvocation(value), guard, goals)

        fun ofAchievementGoalFailure(
            value: Struct,
            goals: List<Goal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(AchievementGoalFailure(value), guard, goals)

        fun ofTestGoalInvocation(
            value: Struct,
            goals: List<Goal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(TestGoalInvocation(value), guard, goals)

        fun ofTestGoalFailure(
            value: Struct,
            goals: List<Goal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(TestGoalFailure(value), guard, goals)
    }
}
