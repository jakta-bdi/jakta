package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.AchievementGoalFailure
import io.github.anitvam.agents.bdi.events.AchievementGoalInvocation
import io.github.anitvam.agents.bdi.events.BeliefBaseAddition
import io.github.anitvam.agents.bdi.events.BeliefBaseRemoval
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.TestGoalFailure
import io.github.anitvam.agents.bdi.events.TestGoalInvocation
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth

interface Plan {
    val trigger: Trigger
    val guard: Struct
    val goals: List<Goal>

    fun isApplicable(event: Event, beliefBase: BeliefBase): Boolean

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
