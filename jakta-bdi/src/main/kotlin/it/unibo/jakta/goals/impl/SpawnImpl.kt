package it.unibo.jakta.goals.impl

import it.unibo.jakta.goals.Goal
import it.unibo.jakta.goals.Spawn
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

internal class SpawnImpl(override val value: Struct) : Spawn {
    override fun applySubstitution(substitution: Substitution) =
        SpawnImpl(value.apply(substitution).castToStruct())

    override fun toString() = "Achieve($value)"

    override fun copy(value: Struct): Goal = SpawnImpl(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Spawn
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
