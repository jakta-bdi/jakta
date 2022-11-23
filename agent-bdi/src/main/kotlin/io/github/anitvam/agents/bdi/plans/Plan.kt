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
    val id: PlanID
    val trigger: Trigger
    val guard: Struct
    val goals: List<Goal>

    fun isApplicable(event: Event, beliefBase: BeliefBase): Boolean

    fun toActivationRecord(): ActivationRecord

    companion object {
        private fun of(id: PlanID, trigger: Trigger, guard: Struct, goals: List<Goal>): Plan =
            PlanImpl(id, trigger, guard, goals)

        fun ofBeliefBaseAddition(
            belief: Belief,
            id: PlanID = PlanID(),
            guard: Struct = Truth.TRUE,
            goals: List<Goal> = emptyList()
        ): Plan = of(id, BeliefBaseAddition(belief), guard, goals)

        fun ofBeliefBaseRemoval(
            belief: Belief,
            id: PlanID = PlanID(),
            guard: Struct = Truth.TRUE,
            goals: List<Goal> = emptyList()
        ): Plan = of(id, BeliefBaseRemoval(belief), guard, goals)

        fun ofAchievementGoalInvocation(
            value: Struct,
            id: PlanID = PlanID(),
            guard: Struct = Truth.TRUE,
            goals: List<Goal> = emptyList()
        ): Plan = of(id, AchievementGoalInvocation(value), guard, goals)

        fun ofAchievementGoalFailure(
            value: Struct,
            id: PlanID = PlanID(),
            guard: Struct = Truth.TRUE,
            goals: List<Goal> = emptyList()
        ): Plan = of(id, AchievementGoalFailure(value), guard, goals)

        fun ofTestGoalInvocation(
            value: Struct,
            id: PlanID = PlanID(),
            guard: Struct = Truth.TRUE,
            goals: List<Goal> = emptyList()
        ): Plan = of(id, TestGoalInvocation(value), guard, goals)

        fun ofTestGoalFailure(
            value: Struct,
            id: PlanID = PlanID(),
            guard: Struct = Truth.TRUE,
            goals: List<Goal> = emptyList()
        ): Plan = of(id, TestGoalFailure(value), guard, goals)
    }
}
