package io.github.anitvam.agents.bdi.intentions.impl

import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionID
import it.unibo.tuprolog.core.Substitution

internal class IntentionImpl(
    override val recordStack: List<ActivationRecord>,
    override val isSuspended: Boolean = false,
    override val id: IntentionID = IntentionID(),
) : Intention {

    override fun pop(): Intention {
        val record = recordStack.first()
        return if (record.isLastGoal()) {
            this.copy(recordStack = recordStack - record)
        } else {
            this.copy(recordStack = listOf(record.pop()) + recordStack - record)
        }
    }

    override fun push(activationRecord: ActivationRecord) =
        this.copy(recordStack = listOf(activationRecord) + recordStack)

    override fun applySubstitution(substitution: Substitution): Intention {
        val record = recordStack.first()
        return this.copy(recordStack = listOf(record.applySubstitution(substitution)) + recordStack - record)
    }
}
