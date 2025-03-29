package it.unibo.jakta.agents.fsm.time

data class SimulatedTime(
    val value: Double,
) : Time {
    override fun compareTo(other: Time): Int =
        when (other) {
            is SimulatedTime -> value.compareTo(other.value)
            else -> throw IllegalArgumentException("Cannot compare SimulatedTime with $other")
        }
}
