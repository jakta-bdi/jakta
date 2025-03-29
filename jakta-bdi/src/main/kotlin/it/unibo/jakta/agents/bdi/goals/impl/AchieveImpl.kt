package it.unibo.jakta.agents.bdi.goals.impl

import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class AchieveImpl(
    override val value: Struct,
) : Achieve {
    override fun applySubstitution(substitution: Substitution) = AchieveImpl(value.apply(substitution).castToStruct())

    override fun toString(): String = "Achieve($value)"

    override fun copy(value: Struct): Goal = AchieveImpl(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AchieveImpl
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
