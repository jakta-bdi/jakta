package it.unibo.jakta.intentions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.plans.ExecutionResult
import it.unibo.jakta.plans.Plan

interface Intention<ActionResult> where
    ActionResult: ExecutionResult<Any>
{
    val recordStack: List<ActivationRecord<ActionResult>>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextTask(): Action<Any?, Any, ActionResult>? =
        recordStack.firstOrNull()?.actionsQueue?.firstOrNull()

    fun currentPlan(): Plan<ActionResult> = recordStack.first().generatingPlan

    /**
     * Removes the first task to be executed from the first activation record.
     * If the goal is the last one,
     * then the whole activation record is removed from the records stack.
     * @return the [Task] removed.
     */
    fun pop(): Action<Any?, Any, ActionResult>?

    /**
     * Inserts at the top of the records stack the new [ActivationRecord].
     * @param activationRecord the [ActivationRecord] that is inserted in the records stack.
     * @return true if the intention is modified, otherwise false
     */
    fun push(activationRecord: ActivationRecord<ActionResult>): Boolean
}
