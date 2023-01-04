package io.github.anitvam.agents.bdi.goals.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth

internal class AddBeliefImpl(private val addedBelief: Belief) : AddBelief {
    override val value: Struct
        get() = addedBelief.rule.head
    override fun applySubstitution(substitution: Substitution) =
        AddBeliefImpl(addedBelief.applySubstitution(substitution))

    override fun toString(): String = "AddBelief($value)"

    override fun copy(value: Struct): Goal =
        AddBeliefImpl(Belief.of(value, listOf(Truth.TRUE), true))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AddBelief
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
