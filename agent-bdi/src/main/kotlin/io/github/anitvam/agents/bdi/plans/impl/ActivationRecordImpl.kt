package io.github.anitvam.agents.bdi.plans.impl

import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.github.anitvam.agents.bdi.plans.PlanID
import it.unibo.tuprolog.core.Substitution
import org.gradle.api.artifacts.DependencySubstitutions

internal data class ActivationRecordImpl(
    override val goalQueue: List<Goal>,
    override val plan: PlanID = PlanID(),
) : ActivationRecord {

    override fun pop(): ActivationRecord = copy(goalQueue = goalQueue - goalQueue.first())

    override fun applySubstitution(substitution: Substitution): ActivationRecord =
        copy(goalQueue = goalQueue.map { it.applySubstitution(substitution) })
}