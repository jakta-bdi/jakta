package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.ExecutionResult
import it.unibo.jakta.plans.Plan

interface ActivationRecord<
    Request,
    SideEffect,
    Result: ExecutionResult<SideEffect>,
    Query: Any,
    Belief,
    Event
> {

    val actionsQueue: List<Action<Request, SideEffect, Result>> // = plan.tasks

    val generatingPlan: Plan<Query, Belief, Event>

    fun isLastActionToExecute(): Boolean = actionsQueue.size == 1

    fun pop(): Action<*, *, *>?
}
