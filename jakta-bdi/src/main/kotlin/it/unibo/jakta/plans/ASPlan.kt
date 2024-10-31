package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.tuprolog.core.Struct

interface ASPlan : Plan<Struct, ASBelief> {

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
            goals: List<ASTask<*>>,
        ): ASPlan =
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
