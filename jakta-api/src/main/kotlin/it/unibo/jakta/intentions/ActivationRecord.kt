package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.ExecutionResult
import it.unibo.jakta.plans.Plan

interface ActivationRecord<

    Query: Any,
    Belief,
    Event
> {

    fun <Request, SideEffect, Result: ExecutionResult<SideEffect>> getActionsQueue(): List<Action<Request, SideEffect, Result>> // = plan.tasks

    val generatingPlan: Plan<Query, Belief, Event>

    fun isLastActionToExecute(): Boolean

    fun pop(): Action<*, *, *>?
}
