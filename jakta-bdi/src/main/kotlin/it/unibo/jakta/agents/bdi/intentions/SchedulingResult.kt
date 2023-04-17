package it.unibo.jakta.agents.bdi.intentions

data class SchedulingResult(
    val newIntentionPool: IntentionPool,
    val intentionToExecute: Intention,
)
