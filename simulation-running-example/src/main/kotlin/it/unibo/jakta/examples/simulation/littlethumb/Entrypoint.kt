@file:JvmName("Entrypoint")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.utils.fix
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.dsl.alchemistmas
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

object Grid {
    const val WIDTH = 20.0
    const val HEIGHT = 3.0
}

var signX = 1
var signY = 1

fun <P : Position<P>> movementInGrid(
    environmentForAlchemist: JaktaEnvironmentForAlchemist<P>,
    delta: Double = 0.05,
): P {
    val initialPosition = environmentForAlchemist.alchemistEnvironment.getPosition(environmentForAlchemist.node)
    return if (environmentForAlchemist.randomGenerator.nextBoolean()) {
        addToMyPosition(environmentForAlchemist.alchemistEnvironment, initialPosition, deltaX = delta)
    } else {
        addToMyPosition(environmentForAlchemist.alchemistEnvironment, initialPosition, deltaY = delta)
    }
}

fun <P : Position<P>> addToMyPosition(
    alchemistEnvironment: Environment<*, P>,
    initialPosition: Position<P>,
    deltaX: Double = 0.0,
    deltaY: Double = 0.0,
): P {
    val newPosX = initialPosition.getCoordinate(0) + deltaX * signX
    val newPosY = initialPosition.getCoordinate(1) + deltaY * signY

    if (newPosX > Grid.WIDTH || newPosX < 0.0) {
        signX *= -1
    }

    if (newPosY > Grid.HEIGHT || newPosY < 0.0) {
        signY *= -1
    }

    return alchemistEnvironment.makePosition(
        initialPosition.getCoordinate(0) + deltaX * signX,
        initialPosition.getCoordinate(1) + deltaY * signY,
    )
}

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.releasing(): Agent =
    alchemistmas {
        environment {
            actions {
                action("put", 0) {
                    @Suppress("UNCHECKED_CAST")
                    val castEnv = this.environment as JaktaEnvironmentForAlchemist<P>
                    val alchemistEnv = castEnv.alchemistEnvironment
                    val position = alchemistEnv.getPosition(castEnv.node)
                    if (castEnv.randomGenerator.nextDouble() < 0.15) {
                        alchemistEnv.addNode(
                            GenericNode(alchemistEnv).also {
                                it.setConcentration(SimpleMolecule("breadCrumb"), it.id to position)
                            },
                            position,
                        )
                    }
                }
                action("move", 0) {
                    val mypos = alchemistEnvironment.getPosition(node)
                    alchemistEnvironment.moveNodeToPosition(
                        node,
                        alchemistEnvironment.makePosition(
                            mypos.getCoordinate(0) + 0.05,
                            mypos.getCoordinate(1),
                        ),
                    )
                }
            }
        }
        agent("pollicina") {
            node.setConcentration(SimpleMolecule("agent"), "pollicina")
            beliefs {
                fact { "state"("running") }
            }
            goals {
                achieve("run")
                achieve("releaseObject")
            }
            plans {
                +achieve("run") onlyIf { "state"("running").fromSelf } then {
                    execute("move")
                    achieve("run")
                }

                +achieve("releaseObject") onlyIf { "state"("running").fromSelf } then {
                    execute("put")
                    achieve("releaseObject")
                }

                +"stop"("source"(P)) then {
                    execute("print"("STOP"))
                    -"state"("running").fromSelf
                }
            }
        }
    }

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.catching(sightRadius: Double): Agent =
    alchemistmas {
        environment {
            actions {
                action("move", 0) {
                    @Suppress("UNCHECKED_CAST")
                    val castEnv = this.environment as JaktaEnvironmentForAlchemist<P>
                    alchemistEnvironment.moveNodeToPosition(
                        castEnv.node,
                        movementInGrid(castEnv),
                    )
                }
                action("goTo", 1) {
                    val arg = arguments.first().fix<Pair<Int, Position<*>>>()
                    val nodeId = arg.first
                    val position = arg.second
                    if (alchemistEnvironment.getNodeByID(nodeId).contents.isNotEmpty()) {
                        println("Removing node $nodeId")
                        //   alchemistEnvironment.removeNode(alchemistEnvironment.getNodeByID(nodeId))
                        alchemistEnvironment.getNodeByID(nodeId).removeConcentration(SimpleMolecule("breadCrumb"))
                        alchemistEnvironment.moveNodeToPosition(
                            node,
                            alchemistEnvironment.makePosition(
                                position.getCoordinate(0),
                                position.getCoordinate(1),
                            ),
                        )
                    } else {
                        println("I'm trying to remove not existing node $nodeId")
                    }
                }
                action("stopPollicina", 1) {
                    val name = arguments.first().fix<String>()
                    val payload: Struct = Atom.of("stop")
                    sendMessage("$name@1", Message(sender, Tell, payload))
                }
            }
        }

        agent("pollicino") {
            node.setConcentration(SimpleMolecule("sightRadius"), sightRadius)
            beliefs {
                fact { "state"("running") }
            }
            goals { achieve("catch") }
            plans {
                +achieve("catch") onlyIf {
                    "state"("running").fromSelf and "found"(P).fromSelf
                } then {
                    execute("goTo"(P))
                    -"found"(P).fromSelf
                    achieve("catch")
                }

                +achieve("catch") onlyIf { "state"("running").fromSelf } then {
                    execute("move")
                    achieve("catch")
                }

                +"breadCrumb"(P).fromPercept then {
                    execute("print"("Individuata briciola", P))
                    -"breadCrumb"(P).fromPercept
                    +"found"(P)
                }

                +"agent"(N).fromPercept then {
                    execute("stopPollicina"(N))
                    -"state"("running").fromSelf
                }
            }
        }
    }
