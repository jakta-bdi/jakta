@file:JvmName("Catching")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.dsl.alchemistmas

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.catchingEntrypoint(): Agent =
    CustomEnvironmentForEventDrivenSimulation(this).catching()

fun <P : Position<P>> CustomEnvironmentForEventDrivenSimulation<P>.catching(): Agent =
    alchemistmas {
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
                    execute("stopMessage"(N))
                    -"state"("running").fromSelf
                }
            }
        }
    }
