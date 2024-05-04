@file:JvmName("Entrypoint")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.dsl.alchemistmas

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.releasing(): Agent =
    alchemistmas {
        environment {
            actions {
                action("put", 0) {
                    @Suppress("UNCHECKED_CAST")
                    val castEnv = this.environment as JaktaEnvironmentForAlchemist<P>
                    val alchemistEnv = castEnv.alchemistEnvironment
                    alchemistEnv.addNode(
                        GenericNode(alchemistEnv),
                        alchemistEnv.getPosition(castEnv.node),
                    )
                }
                action("moveRight", 0) {
                    @Suppress("UNCHECKED_CAST")
                    val castEnv = this.environment as JaktaEnvironmentForAlchemist<P>
                    val alchemistEnv = castEnv.alchemistEnvironment
                    val initialPosition = alchemistEnv.getPosition(castEnv.node)
                    alchemistEnvironment.moveNodeToPosition(
                        castEnv.node,
                        alchemistEnv.makePosition(
                            initialPosition.getCoordinate(1) + 0.2,
                            initialPosition.getCoordinate(0),
                        ),
                    )
                }
            }
        }
        agent("pollicina") {
            goals {
                achieve("run")
                achieve("releaseObject")
            }
            plans {
                +achieve("run") then {
                    execute("moveRight")
                    achieve("run")
                }

                +achieve("releaseObject") then {
                    execute("put")
                    achieve("releaseObject")
                }
            }
        }
    }

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.catching(sightRadius: Double): Agent =
    alchemistmas {
        environment {
            actions {
                action("moveRight", 0) {
                    @Suppress("UNCHECKED_CAST")
                    val castEnv = this.environment as JaktaEnvironmentForAlchemist<P>
                    val alchemistEnv = castEnv.alchemistEnvironment
                    val initialPosition = alchemistEnv.getPosition(castEnv.node)
                    alchemistEnvironment.moveNodeToPosition(
                        castEnv.node,
                        alchemistEnv.makePosition(
                            initialPosition.getCoordinate(1) + 0.1,
                            initialPosition.getCoordinate(0),
                        ),
                    )
                }
            }
        }
        agent("pollicino") {
            node.setConcentration(SimpleMolecule("sightRadius"), sightRadius)

            goals {
                achieve("catch")
            }
            plans {
                +achieve("catch") then {
                    execute("moveRight")
                    achieve("catch")
                }
            }
        }
    }
