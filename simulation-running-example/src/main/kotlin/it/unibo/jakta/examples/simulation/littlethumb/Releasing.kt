@file:JvmName("Releasing")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.dsl.alchemistmas

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.releasingEntrypoin(): Agent =
    CustomEnvironmentForEventDrivenSimulation(this).releasing()

fun <P : Position<P>> CustomEnvironmentForEventDrivenSimulation<P>.releasing(): Agent =
    alchemistmas {
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
