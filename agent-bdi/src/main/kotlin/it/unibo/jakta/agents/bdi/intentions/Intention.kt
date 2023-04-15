package it.unibo.jakta.agents.bdi.intentions

import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.jakta.agents.bdi.intentions.impl.IntentionImpl
import it.unibo.jakta.agents.bdi.plans.ActivationRecord
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

interface Intention {
    val recordStack: List<ActivationRecord>

    val isSuspended: Boolean

    val id: IntentionID

    fun nextGoal(): Goal = recordStack.first().goalQueue.first()
    fun currentPlan(): Struct = recordStack.first().plan

    /**
     * Removes the first goal to be executed from the first activation record. If the goal is the last one,
     * then the whole activation record is removed from the records stack.
     */
    fun pop(): Intention

    fun push(activationRecord: ActivationRecord): Intention

    fun applySubstitution(substitution: Substitution): Intention

    fun copy(
        recordStack: List<ActivationRecord> = this.recordStack,
        isSuspended: Boolean = this.isSuspended,
        id: IntentionID = this.id,
    ): Intention = of(recordStack, isSuspended, id)

    companion object {
        fun of(plan: Plan): Intention = IntentionImpl(listOf(plan.toActivationRecord()))

        fun of(
            recordStack: List<ActivationRecord> = emptyList(),
            isSuspended: Boolean = false,
            id: IntentionID = IntentionID(),
        ): Intention = IntentionImpl(recordStack, isSuspended, id)
    }
}
