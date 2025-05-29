package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.impl.IntentionImpl
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

interface ASIntention : Intention<ASBelief, Struct, Solution> {

    override val stack: List<ActivationRecord<ASBelief, Struct, Solution>>

    val isSuspended: Boolean

    override val id: IntentionID

    fun nextTask(): Action<ASBelief, Struct, Solution>? =
        stack.firstOrNull()?.queue?.firstOrNull()

    fun currentPlan(): Plan<ASBelief, Struct, Solution> = stack.first().origin

    /**
     * Removes the first task to be executed from the first activation record.
     * If the goal is the last one,
     * then the whole activation record is removed from the records stack.
     * @return the [Task] removed.
     */
    fun pop(): ASIntention

    /**
     * Inserts at the top of the records stack the new [ActivationRecord].
     * @param activationRecord the [ActivationRecord] that is inserted in the records stack.
     * @return true if the intention is modified, otherwise false
     */
    fun push(activationRecord: ActivationRecord<ASBelief, Struct, Solution>): Boolean

    fun nextActionToExecute(): Action<ASBelief, Struct, Solution>?

    fun applySubstitution(substitution: Substitution): ASIntention

    fun copy(
        recordStack: MutableList<ActivationRecord<ASBelief, Struct, Solution>> = this.stack.toMutableList(),
        isSuspended: Boolean = this.isSuspended,
        id: IntentionID = this.id,
    ): ASIntention = of(recordStack, isSuspended, id)

    companion object {
        fun of(plan: Plan<ASBelief, Struct, Solution>): ASIntention = IntentionImpl(mutableListOf(plan.toActivationRecord()))

        fun of(
            recordStack: MutableList<ActivationRecord<ASBelief, Struct, Solution>> = mutableListOf(),
            isSuspended: Boolean = false,
            id: IntentionID = IntentionID(),
        ): ASIntention = IntentionImpl(recordStack, isSuspended, id)
    }
}
