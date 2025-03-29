package it.unibo.jakta.agents.bdi.plans.impl

import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.jakta.agents.bdi.plans.ActivationRecord
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal data class ActivationRecordImpl(
    override val goalQueue: List<Goal>,
    override val plan: Struct,
) : ActivationRecord {
    override fun pop(): ActivationRecord = copy(goalQueue = goalQueue - goalQueue.first())

    override fun applySubstitution(substitution: Substitution): ActivationRecord =
        copy(goalQueue = goalQueue.map { it.applySubstitution(substitution) })

    override fun isLastGoal(): Boolean = goalQueue.size == 1
}
