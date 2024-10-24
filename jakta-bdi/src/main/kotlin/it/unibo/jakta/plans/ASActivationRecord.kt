package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

data class ASActivationRecord(
    override val plan: ASPlan,
    override val taskQueue: List<ASTask<*>> = listOf(),
) : ActivationRecord<Struct, PrologBelief>{

    override fun pop(): ASActivationRecord = this(plan, taskQueue - taskQueue.first())

    fun isLastGoal(): Boolean = taskQueue.size == 1

    fun applySubstitution(substitution: Substitution): ASActivationRecord = this(
        plan,
        taskQueue.map { it.applySubstitution(substitution) }
    )
}