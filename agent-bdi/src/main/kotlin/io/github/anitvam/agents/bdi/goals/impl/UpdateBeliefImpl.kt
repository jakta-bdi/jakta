package io.github.anitvam.agents.bdi.goals.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth

internal class UpdateBeliefImpl(private val updatedBelief: Belief) : UpdateBelief {
    override val value: Struct
        get() = updatedBelief.rule.head

    override fun applySubstitution(substitution: Substitution) =
        UpdateBeliefImpl(updatedBelief.applySubstitution(substitution))

    override fun toString(): String = "UpdateBelief($updatedBelief)"

    override fun copy(value: Struct): Goal =
        UpdateBeliefImpl(Belief.of(value, listOf(Truth.TRUE), true))
}
