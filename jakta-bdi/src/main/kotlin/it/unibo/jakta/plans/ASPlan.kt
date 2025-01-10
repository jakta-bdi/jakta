package it.unibo.jakta.plans

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.TestGoalFailure
import it.unibo.jakta.events.TestGoalInvocation
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth

interface ASPlan : Plan<Struct, ASBelief, ASEvent> {

    val trigger: ASEvent

    val guard: Struct

    /** Returns the computed applicable plan */
    fun applicablePlan(event: ASEvent, beliefBase: ASBeliefBase): ASPlan

    /**
     * Transforms the current plan into an [ActivationRecord] for the [Intention] that will execute it.
     */
    fun toActivationRecord(): ASActivationRecord

    companion object {
        private fun of(
            trigger: ASEvent,
            guard: Struct,
            goals: List<ASAction<*, *, *>>,
        ): ASPlan = PlanImpl(trigger, guard, goals)

        fun ofBeliefBaseAddition(
            belief: ASBelief,
            goals: List<ASAction<*, *, *>>,
            guard: Struct = Truth.TRUE,
        ): ASPlan = of(BeliefBaseAddition(belief), guard, goals)

        fun ofBeliefBaseRemoval(
            belief: ASBelief,
            goals: List<ASAction<*, *, *>>,
            guard: Struct = Truth.TRUE,
        ): ASPlan = of(BeliefBaseRemoval(belief), guard, goals)

        fun ofAchievementGoalInvocation(
            value: Struct,
            goals: List<ASAction<*, *, *>>,
            guard: Struct = Truth.TRUE,
        ): ASPlan = of(AchievementGoalInvocation(value), guard, goals)

        fun ofAchievementGoalFailure(
            value: Struct,
            goals: List<ASAction<*, *, *>>,
            guard: Struct = Truth.TRUE,
        ): ASPlan = of(AchievementGoalFailure(value), guard, goals)

        fun ofTestGoalInvocation(
            value: Struct,
            goals: List<ASAction<*, *, *>>,
            guard: Struct = Truth.TRUE,
        ): ASPlan = of(TestGoalInvocation(value), guard, goals)

        fun ofTestGoalFailure(
            value: Struct,
            goals: List<ASAction<*, *, *>>,
            guard: Struct = Truth.TRUE,
        ): ASPlan = of(TestGoalFailure(value), guard, goals)
    }
}


