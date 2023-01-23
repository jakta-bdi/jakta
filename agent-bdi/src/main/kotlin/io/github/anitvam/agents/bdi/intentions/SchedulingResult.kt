package io.github.anitvam.agents.bdi.intentions

data class SchedulingResult(
    val newIntentionPool: IntentionPool,
    val intentionToExecute: Intention,
)
