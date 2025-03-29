package it.unibo.jakta.agents.bdi.goals.impl

import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class ActInternallyImpl(
    override val value: Struct,
) : ActInternally {
    override fun applySubstitution(substitution: Substitution) =
        ActInternallyImpl(value.apply(substitution).castToStruct())

    override fun toString(): String = "ActInternally($value)"

    override fun copy(value: Struct) = ActInternallyImpl(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ActInternally
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
