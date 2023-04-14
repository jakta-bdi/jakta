package io.github.anitvam.agents.bdi.goals.impl

import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActExternally
import io.github.anitvam.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

class ActExternallyImpl(override val value: Struct) : ActExternally {
    override fun applySubstitution(substitution: Substitution) =
        ActExternallyImpl(value.apply(substitution).castToStruct())

    override fun toString(): String = "ActExternally($value)"

    override fun copy(value: Struct): Goal = ActExternallyImpl(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Act
        if (value != other.value) return false
        return true
    }
}
