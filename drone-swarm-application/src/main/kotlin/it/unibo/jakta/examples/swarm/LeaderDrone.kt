@file:JvmName("LeaderDrone")

package it.unibo.jakta.examples.swarm

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.util.fix
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.dsl.mas
import it.unibo.jakta.examples.swarm.CircleMovement.createCircleCenter
import it.unibo.jakta.examples.swarm.CircleMovement.degreesToRadians
import it.unibo.jakta.examples.swarm.CircleMovement.positionInCircumference
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.libs.oop.ObjectRef

var leaderCircumferenceDegrees = 180

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.leader(): Agent =
    mas {
        environment {
            actions {
                action("circleMovementStep", 0) {
                    val initialPosition = SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))
                    val radius = data["radius"] ?: error("Missing radius as Node molecule. $data")
                    var center = data["centerPosition"]
                    if (center == null) {
                        center = createCircleCenter(SwarmPosition.fromPosition(initialPosition), radius as Double)
                        addData("centerPosition", center)
                    }
                    val nextPosition = positionInCircumference(
                        data["radius"] as Double,
                        degreesToRadians(leaderCircumferenceDegrees).also {
                            leaderCircumferenceDegrees = ((leaderCircumferenceDegrees + 1) % 360) + 1
                        },
                        SwarmPosition.fromPosition(center),
                    )

                    val movement = nextPosition - initialPosition
                    addData("velocity", doubleArrayOf(movement.x, movement.y))

                    alchemistEnvironment.moveNodeToPosition(
                        node,
                        nextPosition.toPosition(alchemistEnvironment),
                    )
                }
                action("notifyAgent", 1) {
                    val dest = arguments.first().fix<String>()
                    var participants = (data["participants"] ?: setOf<String>()) as Set<*>
                    participants = participants + dest.split("@")[1]
                    updateData("participants" to participants)
                    val payload: Struct = Struct.of(
                        "joinCircle",
                        ObjectRef.of(SwarmPosition.fromPosition(alchemistEnvironment.getPosition(node))),
                        ObjectRef.of(data["radius"]),
                        ObjectRef.of(participants),
                    )
                    sendMessage(dest, Message(sender, Achieve, payload))
                }
            }
        }
        agent("leader") {
            addData("id", node.id)
            goals {
                achieve("move")
            }
            plans {
                +achieve("move") then {
                    // execute("print"("Hi, I'm the leader at node $node.id"))
                    execute("circleMovementStep")
                    execute("sleep"(2000))
                    achieve("move")
                }

                +"agent"(N).fromPercept then {
                    execute("notifyAgent"(N))
                    -"agent"(N).fromPercept
                }
            }
        }
    }
