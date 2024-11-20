package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface ActivationRecord<Query, Belief, Event, PlanType> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>
{

    val taskQueue: List<Task<Query, Belief, *, *>> // = plan.tasks

    val plan: PlanType

    fun isLastTask(): Boolean = taskQueue.size == 1

    fun pop(): Task<Query, Belief, *, *>?
}
