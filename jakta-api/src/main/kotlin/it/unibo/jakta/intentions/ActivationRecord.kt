package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.ExecutionResult
import it.unibo.jakta.plans.Plan

interface ActivationRecord<Result: ExecutionResult<Any>> {

    val actionsQueue: List<Action<Any?, Any, Result>> // = plan.tasks

    val generatingPlan: Plan<Result>

    fun isLastActionToExecute(): Boolean

    fun pop(): Action<Any?, Any, Result>?
}
