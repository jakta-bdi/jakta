package it.unibo.jakta.intentions

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Task
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

data class ASActivationRecord(
    override val plan: ASPlan,
    override val taskQueue: List<Task<*>> = plan.tasks,
) : ActivationRecord<Struct, ASBelief> {

    override fun pop(): ASActivationRecord = ASActivationRecord(plan, taskQueue - taskQueue.first())

    fun isLastGoal(): Boolean = taskQueue.size == 1

    fun applySubstitution(substitution: Substitution): ASActivationRecord = ASActivationRecord(
        plan,
        taskQueue.map { it.applySubstitution(substitution) },
    )
}
