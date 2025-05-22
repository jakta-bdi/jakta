package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Substitution

data class ASActivationRecord(
    val generatingPlan: ASPlan,
    var taskQueue: List<ASAction>,
) {
    fun isLastActionToExecute(): Boolean = taskQueue.size == 1
    fun pop(): ASAction? = taskQueue.firstOrNull()?.also { taskQueue -= it }
    fun applySubstitution(substitution: Substitution) =
        taskQueue.forEach { it.applySubstitution(substitution) }
}
