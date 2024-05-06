@file:JvmName("Catching")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.jakta.utils.fix
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.dsl.alchemistmas
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.catching(): Agent =
    alchemistmas {
        environment {
            actions {
                action("move", 0) {
                    alchemistEnvironment.moveNodeToPosition(
                        node,
                        movementInGrid(this@catching, pollicinoDirections),
                    )
                }
                action("goTo", 1) {
                    val arg = arguments.first().fix<Pair<Int, Position<*>>>()
                    val nodeId = arg.first
                    val position = arg.second
                    if (alchemistEnvironment.getNodeByID(nodeId).contents.isNotEmpty()) {
                        alchemistEnvironment.getNodeByID(nodeId).removeConcentration(SimpleMolecule("breadCrumb"))
                        alchemistEnvironment.moveNodeToPosition(
                            node,
                            alchemistEnvironment.makePosition(
                                position.getCoordinate(0),
                                position.getCoordinate(1),
                            ),
                        )
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