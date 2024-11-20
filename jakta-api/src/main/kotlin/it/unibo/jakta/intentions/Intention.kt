package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task

interface Intention<Query, Belief, Event, PlanType, ActivationRecordType> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>,
    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>
{
    val recordStack: List<ActivationRecordType>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextTask(): Task<Query, Belief, *, *>? = recordStack.firstOrNull()?.taskQueue?.firstOrNull()

    fun currentPlan(): PlanType = recordStack.first().plan

    /**
     * Removes the first task to be executed from the first activation record.
     * If the goal is the last one,
     * then the whole activation record is removed from the records stack.
     * @return the [Task] removed.
     */
    fun pop(): Task<Query, Belief, *, *>?

    /**
     * Inserts at the top of the records stack the new [ActivationRecord].
     * @param activationRecord the [ActivationRecord] that is inserted in the records stack.
     * @return true if the intention is modified, otherwise false
     */
    fun push(activationRecord: ActivationRecordType): Boolean
}
