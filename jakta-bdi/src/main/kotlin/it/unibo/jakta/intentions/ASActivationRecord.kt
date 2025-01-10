package it.unibo.jakta.intentions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Task
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

data class ASActivationRecord(
    override val plan: ASPlan,
    override var taskQueue: List<Task<Struct, ASBelief, *, *>> = plan.tasks,
) : ActivationRecord<Struct, ASBelief, ASEvent, ASPlan> {

    override fun pop(): Task<Struct, ASBelief, *, *>? =
        taskQueue.firstOrNull()?.also { taskQueue -= it }

    fun applySubstitution(substitution: Substitution) =
        taskQueue.forEach{ if (it is ASAction<*>) it.applySubstitution(substitution)}

}
