package io.github.anitvam.agents.bdi.plans.impl

import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.github.anitvam.agents.bdi.plans.PlanID
import it.unibo.tuprolog.core.Substitution

internal data class ActivationRecordImpl(
    override val goalQueue: List<Goal>,
    override val plan: PlanID = PlanID(),
) : ActivationRecord {

    override fun pop(): ActivationRecord = copy(goalQueue = goalQueue - goalQueue.first())

    override fun applySubstitution(substitution: Substitution): ActivationRecord =
        copy(goalQueue = goalQueue.map { it.applySubstitution(substitution) })

    override fun isLastGoal(): Boolean = goalQueue.size == 1
}
