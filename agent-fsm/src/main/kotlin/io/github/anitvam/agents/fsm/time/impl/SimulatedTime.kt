package io.github.anitvam.agents.fsm.time.impl

import io.github.anitvam.agents.fsm.time.Time

internal class SimulatedTime(val value: Double) : Time {
    override fun compareTo(other: Time): Int = when (other) {
        is SimulatedTime -> value.compareTo(other.value)
        else -> throw IllegalArgumentException("Cannot compare SimulatedTime with $other")
    }
}
