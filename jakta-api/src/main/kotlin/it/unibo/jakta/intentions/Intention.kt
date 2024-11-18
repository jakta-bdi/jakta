package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface Intention<Query : Any, Belief, ActivationRecordType: ActivationRecord<Query, Belief>> {
    val recordStack: List<ActivationRecordType>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextTask(): Task<Query, Belief, *, *> = recordStack.first().taskQueue.first()

    fun currentPlan(): Plan<Query, Belief> = recordStack.first().plan

    /**
     * Removes the first goal to be executed from the first activation record.
     * If the goal is the last one,
     * then the whole activation record is removed from the records stack.
     * @return the [ActivationRecord] removed.
     */
    fun pop(): ActivationRecordType

    /**
     * Inserts at the top of the records stack the new [ActivationRecord].
     * @param activationRecord the [ActivationRecord] that is inserted in the records stack.
     * @return true if the intention is modified, otherwise false
     */
    fun push(activationRecord: ActivationRecordType): Boolean
}
