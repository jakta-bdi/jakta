package io.github.anitvam.agents.bdi.goals.impl

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.goals.Test
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth

internal class TestImpl(private val belief: Belief) : Test {
    override val value: Struct
        get() = belief.rule.head

    override fun applySubstitution(substitution: Substitution) =
        TestImpl(belief.applySubstitution(substitution))

    override fun toString(): String = "Test($value)"

    override fun copy(value: Struct): Goal =
        TestImpl(Belief.of(value, listOf(Truth.TRUE), true))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Test
        if (value != other.value) return false
        return true
    }

    override fun hashCode(): Int = value.hashCode()
}
