package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.Goal
import it.unibo.jakta.goals.PrologGoal
import it.unibo.jakta.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct

interface PrologPlan<T : Trigger<*>, Go : Goal<*>> : Plan<T, Guard<Struct>, Go> {
    companion object {
        private fun <Go : Goal<*>, T : Trigger<*>> of(
            trigger: T,
            guard: Guard<Struct>,
            goals: List<Go>,
        ): PrologPlan<T, Go> =
            PlanImpl(trigger, guard, goals)

        fun ofBeliefBaseAddition(
            belief: Belief,
            goals: List<PrologGoal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(BeliefBaseAddition(belief), guard, goals)

        fun ofBeliefBaseRemoval(
            belief: Belief,
            goals: List<PrologGoal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(BeliefBaseRemoval(belief), guard, goals)

        fun ofAchievementGoalInvocation(
            value: Struct,
            goals: List<PrologGoal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(AchievementGoalInvocation(value), guard, goals)

        fun ofAchievementGoalFailure(
            value: Struct,
            goals: List<PrologGoal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(AchievementGoalFailure(value), guard, goals)

        fun ofTestGoalInvocation(
            value: Struct,
            goals: List<PrologGoal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(TestGoalInvocation(value), guard, goals)

        fun ofTestGoalFailure(
            value: Struct,
            goals: List<PrologGoal>,
            guard: Struct = Truth.TRUE,
        ): Plan = of(TestGoalFailure(value), guard, goals)
    }
}
