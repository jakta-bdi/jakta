package it.unibo.jakta.agents.fsm.time

data class EpochTime(
    val value: Long,
) : Time {
    override fun compareTo(other: Time): Int =
        when (other) {
            is EpochTime -> value.compareTo(other.value)
            else -> throw IllegalArgumentException("Cannot compare EpochTime with $other")
        }
}
