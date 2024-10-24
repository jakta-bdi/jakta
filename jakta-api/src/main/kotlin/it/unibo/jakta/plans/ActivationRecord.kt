package it.unibo.jakta.plans

interface ActivationRecord<out Query : Any, in Belief> {

    val taskQueue: List<Task<*>> // = plan.tasks

    val plan: Plan<Query, Belief>

    fun pop(): ActivationRecord<Query, Belief>

}
