package it.unibo.jakta.situated

import kotlin.math.sqrt

/**
 * A point in a Euclidean space with any number of dimensions, e.g. `Coordinates(1.0, 2.0)`.
 * @property values the coordinates, one per dimension.
 */
data class Coordinates(val values: List<Double>) {
    constructor(vararg values: Double) : this(values.toList())

    /**
     * The Euclidean distance from [other].
     */
    fun distanceTo(other: Coordinates): Double = sqrt((this - other).values.sumOf { it * it })

    /**
     * The coordinates translated by [other].
     */
    operator fun plus(other: Coordinates): Coordinates = combine(other, Double::plus)

    /**
     * The offset from [other] to these coordinates.
     */
    operator fun minus(other: Coordinates): Coordinates = combine(other, Double::minus)

    /**
     * The coordinates scaled by [factor].
     */
    operator fun times(factor: Double): Coordinates = Coordinates(values.map { it * factor })

    override fun toString(): String = values.joinToString(prefix = "(", postfix = ")")

    private fun combine(other: Coordinates, operation: (Double, Double) -> Double): Coordinates {
        require(values.size == other.values.size) { "Cannot combine $this and $other: different dimensions" }
        return Coordinates(values.zip(other.values, operation))
    }
}
