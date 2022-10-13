package io.github.anitvam.agents.bdi.intentions

import io.github.anitvam.agents.bdi.ActivationRecord
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan

interface Intention {
    val recordStack: List<ActivationRecord>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextGoal(): Goal = recordStack.last().goalQueue.first()

    fun pop(): Intention

    fun push(activationRecord: ActivationRecord): Intention
}
