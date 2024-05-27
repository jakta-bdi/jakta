package it.unibo.jakta.examples.swarm

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.jakta.examples.swarm.CircleMovement.degreesToRadians
import kotlin.math.sqrt

class TestMovementManager : FreeSpec({
    "positionInCircumference generate the right position to go to" {
        CircleMovement.positionInCircumference(
            1.0,
            degreesToRadians(45),
            SwarmPosition(0.0, 0.0),
        ) shouldBe SwarmPosition(
            sqrt(2.0) / 2.0,
            sqrt(2.0) / 2.0,
        )
    }
})
