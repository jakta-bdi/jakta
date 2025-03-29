package it.unibo.jakta.agents.bdi.goals.impl

import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class ActImpl(
    override val value: Struct,
) : Act {
    override fun applySubstitution(substitution: Substitution) = ActImpl(value.apply(substitution).castToStruct())

    override fun toString(): String = "Act($value)"

    override fun copy(value: Struct): Goal = ActImpl(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Act
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
