package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.ExecutionResult
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.Task
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

data class ASActivationRecord(
    override val generatingPlan: ASPlan,
    var taskQueue: List<ASAction> = generatingPlan.tasks,
) : ActivationRecord<Struct, ASBelief, ASEvent> {

    override fun <Request, SideEffect, Result : ExecutionResult<SideEffect>> getActionsQueue():
        List<Action<Request, SideEffect, Result>> =
        taskQueue


    override fun isLastActionToExecute(): Boolean = taskQueue.size == 1


    override fun pop(): ASAction? =
        taskQueue.firstOrNull()?.also { taskQueue -= it }

    fun applySubstitution(substitution: Substitution) =
        taskQueue.forEach{ it.applySubstitution(substitution)}

}
