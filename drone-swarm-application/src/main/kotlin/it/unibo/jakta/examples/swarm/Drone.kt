@file:JvmName("Drone")

package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.dsl.mas
import it.unibo.jakta.examples.swarm.CircleMovement.positionInCircumference
import it.unibo.tuprolog.solve.libs.oop.ObjectRef
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.drone() =
    mas {
        environment {
            actions {
                action("follow", 3) {
                    val center = argument<ObjectRef>(0).fix<SwarmPosition>()
                    val radius = argument<ObjectRef>(1).fix<Double>()
                    val otherNodes = argument<ObjectRef>(2).fix<Set<String>>()
                    val myPosition = SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))

                    // Compute my destination in the circle
                    val angles = (2 * PI) / otherNodes.count()
                    val destinationAngle = otherNodes.sorted().indexOf(node.id.toString()) * angles
                    val destinationPosition = CircleMovement.positionInCircumference(
                        radius,
                        destinationAngle,
                        center,
                    )
                    // Compute the movement to perform
                    val movement = destinationPosition - myPosition
                    addData("velocity", doubleArrayOf(movement.x, movement.y))
                    val distanceToMove = hypot(movement.x, movement.y)
                    val maxPossibleMovementRadius = 0.15
                    if (maxPossibleMovementRadius >= distanceToMove) {
                        alchemistEnvironment.moveNodeToPosition(
                            node,
                            destinationPosition.toPosition(alchemistEnvironment),
                        )
                    } else {
                        val angleToReachDestination = atan2(movement.y, movement.x)
                        val actualDestination = positionInCircumference(
                            maxPossibleMovementRadius,
                            angleToReachDestination,
                            myPosition,
                        )
                        alchemistEnvironment.moveNodeToPosition(
                            node,
                            actualDestination.toPosition(alchemistEnvironment),
                        )
                    }
                }
            }
        }
        agent("drone") {
            addData("id", node.id)
            addData("agent", "drone@${node.id}")
            plans {
                +achieve("joinCircle"(C, R, N)) then {
                    // execute("print"("I'm node ${node.id} and I'm joining cluster"))
                    execute("follow"(C, R, N))
                }
            }
        }
    }
