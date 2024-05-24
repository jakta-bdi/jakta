package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object CircleMovementManager {
    fun createCircleCenter(position: SwarmPosition, radius: Double): SwarmPosition =
        SwarmPosition(
            position.x + radius,
            position.y,
        )

    fun positionInCircumference(
        radius: Double,
        radians: Double,
        center: SwarmPosition,
    ): SwarmPosition = SwarmPosition(
        radius * cos(radians) + center.x,
        radius * sin(radians) + center.y,
    )

    fun degreesToRadians(degrees: Int): Double {
        return degrees * (PI / 180)
    }
}

data class SwarmPosition(val x: Double, val y: Double) {
    fun <P : Position<P>> toPosition(alchemistEnvironment: Environment<*, P>): P =
        alchemistEnvironment.makePosition(x, y)

    companion object {
        fun fromDoubleArray(doubleArray: DoubleArray): SwarmPosition =
            SwarmPosition(doubleArray[0], doubleArray[1])

        fun fromPosition(position: Any): SwarmPosition {
            if (position is Position<*>) {
                return fromDoubleArray(position.coordinates)
            } else if (position is SwarmPosition) {
                return position
            }
            error("Argument is not a Position<*>, don't know what to do with it. position is ${position.javaClass}")
        }
    }
}
