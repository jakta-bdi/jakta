package io.github.anitvam.agents.fsm.time

import io.github.anitvam.agents.fsm.time.impl.SimulatedTime
import io.github.anitvam.agents.fsm.time.impl.EpochTime

interface Time : Comparable<Time> {
    companion object {
        fun of(value: Double): Time = SimulatedTime(value)
        fun of(value: Int): Time = SimulatedTime(value.toDouble())
        fun of(value: Long): Time = EpochTime(value)
    }
}
