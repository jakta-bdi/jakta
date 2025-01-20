package it.unibo.jakta.intentions.impl

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.IntentionID
import it.unibo.tuprolog.core.Substitution

internal class IntentionImpl(
    override var recordStack: List<ASActivationRecord> = listOf(),
    override val isSuspended: Boolean = false,
    override val id: IntentionID = IntentionID(),
) : ASIntention {

    override fun pop(): ASAction? {
        val record = recordStack.firstOrNull() ?: return null
        return if (record.isLastTask()) {
            recordStack = recordStack - record
            return null
        } else {
            record.pop()
        }
    }

    override fun push(activationRecord: ASActivationRecord): Boolean {
        recordStack = listOf(activationRecord) + recordStack
        return true
    }

    override fun applySubstitution(substitution: Substitution) =
        recordStack.first().applySubstitution(substitution)

    override fun toString(): String = "$id { \n ${recordStack.joinToString(separator = "\n", prefix = "\t")} \n }"
}
