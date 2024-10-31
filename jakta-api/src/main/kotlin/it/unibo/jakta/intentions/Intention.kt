package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface Intention<Query : Any, Belief> {
    val recordStack: List<ActivationRecord<Query, Belief>>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextTask(): Task<*> = recordStack.first().taskQueue.first()

    fun currentPlan(): Plan<Query, Belief> = recordStack.first().plan

    /**
     * Removes the first goal to be executed from the first activation record. If the goal is the last one,
     * then the whole activation record is removed from the records stack.
     */
    fun pop(): Intention<Query, Belief>

    fun push(activationRecord: ActivationRecord<Query, Belief>): Intention<Query, Belief>
}
