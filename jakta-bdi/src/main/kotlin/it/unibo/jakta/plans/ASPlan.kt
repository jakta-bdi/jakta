package it.unibo.jakta.plans

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.events.*
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.plans.impl.PlanImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Truth
import it.unibo.tuprolog.solve.Solution

object ASPlan {
    private fun of(
        trigger: Event.Internal,
        guard: Struct,
        goals: List<ASAction>,
    ): ASPlan = PlanImpl(trigger = trigger, guard = guard, tasks = goals)

    fun ofBeliefBaseAddition(
        belief: ASBelief,
        goals: List<ASAction>,
        guard: Struct = Truth.TRUE,
    ): ASPlan = of(BeliefBaseAddition(belief), guard, goals)

    fun ofBeliefBaseRemoval(
        belief: ASBelief,
        goals: List<ASAction>,
        guard: Struct = Truth.TRUE,
    ): ASPlan = of(BeliefBaseRemoval(belief), guard, goals)

    fun ofAchievementGoalInvocation(
        value: Struct,
        goals: List<ASAction>,
        guard: Struct = Truth.TRUE,
    ): ASPlan = of(AchievementGoalInvocation(value), guard, goals)

    fun ofAchievementGoalFailure(
        value: Struct,
        goals: List<ASAction>,
        guard: Struct = Truth.TRUE,
    ): ASPlan = of(AchievementGoalFailure(value), guard, goals)

    fun ofTestGoalInvocation(
        value: Struct,
        goals: List<ASAction>,
        guard: Struct = Truth.TRUE,
    ): ASPlan = of(TestGoalInvocation(value), guard, goals)

    fun ofTestGoalFailure(
        value: Struct,
        goals: List<ASAction>,
        guard: Struct = Truth.TRUE,
    ): ASPlan = of(TestGoalFailure(value), guard, goals)
}
