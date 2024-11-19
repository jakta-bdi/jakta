package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface ActivationRecord<Query : Any, Belief, Event> {

    val taskQueue: List<Task<Query, Belief, *, *>> // = plan.tasks

    val plan: Plan<Query, Belief, Event>

    fun pop(): ActivationRecord<Query, Belief, Event>
}
