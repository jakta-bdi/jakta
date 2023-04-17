package it.unibo.jakta.agents.bdi.goals.impl

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.jakta.agents.bdi.goals.UpdateBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class UpdateBeliefImpl(private val updatedBelief: Belief) : UpdateBelief {
    override val value: Struct
        get() = updatedBelief.rule.head

    override fun applySubstitution(substitution: Substitution) =
        UpdateBeliefImpl(updatedBelief.applySubstitution(substitution))

    override fun toString(): String = "UpdateBelief($updatedBelief)"

    override fun copy(value: Struct): Goal = UpdateBeliefImpl(Belief.from(value)) // TODO("QUI SI ROMPE")
}
