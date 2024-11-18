package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface ActivationRecord<Query : Any, Belief> {

    val taskQueue: List<Task<Query, Belief, *, *>> // = plan.tasks

    val plan: Plan<Query, Belief>

    fun pop(): ActivationRecord<Query, Belief>
}
