package it.unibo.jakta.intentions

data class SchedulingResult(
    val newIntentionPool: IntentionPool,
    val intentionToExecute: Intention,
)
