package io.github.anitvam.agents.bdi.goals.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth

internal class RemoveBeliefImpl(private val removedBelief: Belief) : RemoveBelief {
    override val value: Struct
        get() = removedBelief.rule.head

    override fun applySubstitution(substitution: Substitution) =
        RemoveBeliefImpl(removedBelief.applySubstitution(substitution))

    override fun toString(): String = "RemoveBelief($value)"

    override fun copy(value: Struct): Goal =
        RemoveBeliefImpl(Belief.of(value, listOf(Truth.TRUE), true))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RemoveBelief
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
