package it.unibo.jakta.intentions.impl

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.IntentionID
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

internal class IntentionImpl(
    override var stack: List<ActivationRecord<ASBelief, Struct, Solution>> = listOf(),
    override val isSuspended: Boolean = false,
    override val id: IntentionID = IntentionID(),
) : ASIntention {

    override fun nextActionToExecute(): Action<ASBelief, Struct, Solution>? {
        val record = stack.firstOrNull() ?: return null
        return record.nextActionToExecute()
    }

    override fun pop(): ASIntention {
        val record = stack.firstOrNull() ?: return this
        return if (record.isLastActionToExecute()) {
            IntentionImpl(
                stack - record,
                isSuspended,
                id,
            )
        } else {
            IntentionImpl(
                listOf(record.pop()) + stack - record,
                isSuspended,
                id,
            )
        }
    }

    override fun push(activationRecord: ActivationRecord<ASBelief, Struct, Solution>): Boolean {
        stack = listOf(activationRecord) + stack
        return true
    }

    override fun applySubstitution(substitution: Substitution) =
        IntentionImpl(
            stack.apply { (stack.first() as? ASActivationRecord)?.applySubstitution(substitution) ?: error("Invalid activation record type") },
            isSuspended,
            id,
        )

    override fun toString(): String = "Intention { id = ${id.id} \n ${stack.joinToString(
        separator = "\n\t",
        prefix = "\t",
    )} \n }"
}
