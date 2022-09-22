package io.github.anitvam.agents.bdi

interface Intention {
    val recordStack: List<ActivationRecord>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextGoal(): Goal = recordStack.last().goalQueue.first()

    fun pop(): Intention
}
