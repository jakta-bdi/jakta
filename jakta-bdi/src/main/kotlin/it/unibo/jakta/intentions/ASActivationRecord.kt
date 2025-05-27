package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Substitution

data class ASActivationRecord(
    val generatingPlan: ASPlan,
    val taskQueue: List<ASAction>,
) {
    fun isLastActionToExecute(): Boolean = taskQueue.size == 1
    fun nextActionToExecute(): ASAction? = taskQueue.firstOrNull()
    fun pop(): ASActivationRecord = ASActivationRecord(generatingPlan, when (nextActionToExecute() != null) {
        true -> taskQueue - nextActionToExecute()!!
        false -> taskQueue
    })
    fun applySubstitution(substitution: Substitution) = ASActivationRecord(
        generatingPlan,
        taskQueue.map { it.applySubstitution(substitution) }
    )
}
