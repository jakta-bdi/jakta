package it.unibo.jakta.intentions

import it.unibo.jakta.plans.ExecutionResult

interface IntentionPool<ActionResult: ExecutionResult<Any>> : Map<IntentionID, Intention<ActionResult>> {
    fun nextIntention(): Intention<ActionResult>
}

interface MutableIntentionPool<ActionResult: ExecutionResult<Any>> :
    Map<IntentionID, Intention<ActionResult>>, IntentionPool<ActionResult>
{
    fun updateIntention(intention: Intention<ActionResult>): Boolean
    fun pop(): Intention<ActionResult>?
    fun deleteIntention(intentionID: IntentionID): Intention<ActionResult>?
    fun snapshot(): IntentionPool<ActionResult>
}
