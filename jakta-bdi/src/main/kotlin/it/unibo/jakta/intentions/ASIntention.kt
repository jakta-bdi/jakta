package it.unibo.jakta.intentions

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.intentions.impl.IntentionImpl
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

interface ASIntention : Intention<Struct, ASBelief> {
    fun applySubstitution(substitution: Substitution): ASIntention

//    fun copy(
//        recordStack: List<ASActivationRecord> = this.recordStack,
//        isSuspended: Boolean = this.isSuspended,
//        id: IntentionID = this.id,
//    ): ASIntention = of(recordStack, isSuspended, id)

    companion object {
        fun of(plan: ASPlan): ASIntention = IntentionImpl(listOf(plan.toActivationRecord()))

        fun of(
            recordStack: List<ASActivationRecord> = emptyList(),
            isSuspended: Boolean = false,
            id: IntentionID = IntentionID(),
        ): ASIntention = IntentionImpl(recordStack, isSuspended, id)
    }
}
