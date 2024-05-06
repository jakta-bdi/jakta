@file:JvmName("Releasing")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.dsl.alchemistmas

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.releasing(): Agent =
    alchemistmas {
        environment {
            actions {
                action("put", 0) {
                    val position = alchemistEnvironment.getPosition(node)
                    if (randomGenerator.nextDouble() < 0.15) {
                        alchemistEnvironment.addNode(
                            GenericNode(alchemistEnvironment).also {
                                it.setConcentration(SimpleMolecule("breadCrumb"), it.id to position)
                            },
                            position,
                        )
                    }
                }
                action("move", 0) {
                    alchemistEnvironment.moveNodeToPosition(
                        node,
                        movementInGrid(this@releasing, pollicinaDirections),
                    )
                }
            }
        }
        agent("pollicina") {
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
                    -"state"("running").fromSelf
                }
            }
        }
    }
