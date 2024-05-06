package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

object Grid {
    const val WIDTH = 5.0
    const val HEIGHT = 5.0
}

class Directions(var signX: Int = 1, var signY: Int = 1)
val pollicinaDirections = Directions()
val pollicinoDirections = Directions()

fun <P : Position<P>> movementInGrid(
    environmentForAlchemist: JaktaEnvironmentForAlchemist<P>,
    directions: Directions,
    delta: Double = 0.05,
): P {
    val initialPosition = environmentForAlchemist.alchemistEnvironment.getPosition(environmentForAlchemist.node)
    return if (environmentForAlchemist.randomGenerator.nextBoolean()) {
        addToMyPosition(environmentForAlchemist.alchemistEnvironment, initialPosition, directions, deltaX = delta)
    } else {
        addToMyPosition(environmentForAlchemist.alchemistEnvironment, initialPosition, directions, deltaY = delta)
    }
}

fun <P : Position<P>> addToMyPosition(
    alchemistEnvironment: Environment<*, P>,
    initialPosition: Position<P>,
    directions: Directions,
    deltaX: Double = 0.0,
    deltaY: Double = 0.0,
): P {
    val newPosX = initialPosition.getCoordinate(0) + deltaX * directions.signX
    val newPosY = initialPosition.getCoordinate(1) + deltaY * directions.signY

    if (newPosX > Grid.WIDTH || newPosX < 0.0) {
        directions.signX *= -1
    }

    if (newPosY > Grid.HEIGHT || newPosY < 0.0) {
        directions.signY *= -1
    }

    return alchemistEnvironment.makePosition(
        initialPosition.getCoordinate(0) + deltaX * directions.signX,
        initialPosition.getCoordinate(1) + deltaY * directions.signY,
    )
}
