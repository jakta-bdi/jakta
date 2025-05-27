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

    override fun nextActionToExecute(): ASAction? {
        val record = recordStack.firstOrNull() ?: return null
        return if (record.isLastActionToExecute()) {
            return null
        } else {
            record.nextActionToExecute()
        }
    }

    override fun pop(): ASIntention {
        val record = recordStack.firstOrNull() ?: return this
        return if (record.isLastActionToExecute()) {
            IntentionImpl(
                recordStack - record,
                isSuspended,
                id)
        } else {
            IntentionImpl(
                listOf(record.pop()) + recordStack - record,
                isSuspended,
                id
            )
        }
    }

    override fun push(activationRecord: ASActivationRecord): Boolean {
        recordStack = listOf(activationRecord) + recordStack
        return true
    }

    override fun applySubstitution(substitution: Substitution) =
        IntentionImpl(
            recordStack.apply { recordStack.first().applySubstitution(substitution) },
            isSuspended,
            id
        )

    override fun toString(): String = "$id { \n ${recordStack.joinToString(separator = "\n", prefix = "\t")} \n }"
}
