package it.unibo.jakta.agents.fsm.time

interface Time : Comparable<Time> {
    companion object {
        fun continuous(value: Double): Time = SimulatedTime(value)

        fun discrete(value: Int): Time = SimulatedTime(value.toDouble())

        fun real(value: Long): Time = EpochTime(value)
    }
}
