package io.github.anitvam.agents.fsm.time

import io.github.anitvam.agents.fsm.time.impl.SimulatedTime
import io.github.anitvam.agents.fsm.time.impl.EpochTime

interface Time : Comparable<Time> {

    companion object {

        // TODO: rename fun continous
        fun of(value: Double): Time = SimulatedTime(value)

        // TODO: rename fun discrete
        fun of(value: Int): Time = SimulatedTime(value.toDouble())

        // TODO: rename fun real
        fun of(value: Long): Time = EpochTime(value)
    }
}
