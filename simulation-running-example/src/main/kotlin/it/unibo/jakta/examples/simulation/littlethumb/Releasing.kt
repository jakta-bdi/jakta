@file:JvmName("Releasing")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.dsl.mas
import it.unibo.jakta.examples.simulation.littlethumb.environment.CustomEnvironmentForSimulation

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.simulationReleasing(): Agent =
    mas {
        environment(CustomEnvironmentForSimulation(this@simulationReleasing))
        agents = pollicina()
    }

fun pollicina(): Agent =
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
                // execute("sleep"(1e10.toLong()))
                achieve("run")
            }

            +achieve("releaseObject") onlyIf { "state"("running").fromSelf } then {
                execute("put")
                achieve("releaseObject")
            }

            +"stop"("source"(P)) then {
                execute("print"("RICEVUTO"))
                // -"state"("running").fromSelf
                execute("stop")
            }
        }
    }
