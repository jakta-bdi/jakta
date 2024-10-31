package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface ActivationRecord<out Query : Any, in Belief> {

    val taskQueue: List<Task<*>> // = plan.tasks

    val plan: Plan<Query, Belief>

    fun pop(): ActivationRecord<Query, Belief>
}
