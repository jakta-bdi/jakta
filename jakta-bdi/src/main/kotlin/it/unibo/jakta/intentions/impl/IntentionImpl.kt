package it.unibo.jakta.intentions.impl

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.IntentionID
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal data class IntentionImpl(
    override val recordStack: List<ASActivationRecord>,
    override val isSuspended: Boolean = false,
    override val id: IntentionID = IntentionID(),
) : ASIntention {

    override fun pop(): ASIntention {
        val record = recordStack.first()
        return if (record.isLastGoal()) {
            this.copy(recordStack = recordStack - record)
        } else {
            this.copy(recordStack = listOf(record.pop()) + recordStack - record)
        }
    }

    override fun push(activationRecord: ActivationRecord<Struct, ASBelief>): ASIntention =
        when (activationRecord is ASActivationRecord) {
            true -> this.copy(recordStack = listOf(activationRecord) + recordStack)
            else -> this
        }

    override fun applySubstitution(substitution: Substitution): ASIntention {
        val record = recordStack.first()
        return this.copy(recordStack = listOf(record.applySubstitution(substitution)) + recordStack - record)
    }

    override fun toString(): String = "$id { \n ${recordStack.joinToString(separator = "\n", prefix = "\t")} \n }"
}
