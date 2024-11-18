package it.unibo.jakta.intentions.impl

import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.IntentionID
import it.unibo.tuprolog.core.Substitution

internal class IntentionImpl(
    override var recordStack: List<ASActivationRecord> = listOf(),
    override val isSuspended: Boolean = false,
    override val id: IntentionID = IntentionID(),
) : ASIntention {

    override fun pop(): ASActivationRecord {
        val record = recordStack.first()
        recordStack = if (record.isLastGoal()) {
            recordStack - record
        } else {
            listOf(record.pop()) + recordStack - record
        }
        return record
    }

    override fun push(activationRecord: ASActivationRecord): Boolean {
        recordStack = listOf(activationRecord) + recordStack
        return true
    }

    override fun applySubstitution(substitution: Substitution): Boolean {
        val record = recordStack.first()
        recordStack = listOf(record.applySubstitution(substitution)) + recordStack - record
        return true
    }

    override fun toString(): String = "$id { \n ${recordStack.joinToString(separator = "\n", prefix = "\t")} \n }"
}
