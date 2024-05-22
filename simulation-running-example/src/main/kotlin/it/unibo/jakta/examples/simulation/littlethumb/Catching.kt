@file:JvmName("Catching")

package it.unibo.jakta.examples.simulation.littlethumb

import it.unibo.alchemist.jakta.properties.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.dsl.agent
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromPercept
import it.unibo.jakta.agents.bdi.dsl.beliefs.fromSelf
import it.unibo.jakta.agents.dsl.mas
import it.unibo.jakta.examples.simulation.littlethumb.environment.CustomEnvironmentForSimulation

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.simulationCatching(): Agent =
    mas {
        environment(CustomEnvironmentForSimulation(this@simulationCatching))
        agents = pollicino()
    }

fun pollicino(): Agent =
    agent("pollicino") {
        beliefs {
            fact { "state"("running") }
        }
        goals {
            achieve("greet")
            achieve("catch")
        }
        plans {
            +achieve("greet") then {
                execute("greet")
            }
            +achieve("catch") onlyIf {
                "state"("running").fromSelf and "found"(P).fromSelf
            } then {
                execute("goTo"(P))
                -"found"(P).fromSelf
                achieve("catch")
            }

            +achieve("catch") onlyIf { "state"("running").fromSelf } then {
                execute("move")
                // execute("sleep"(5000.toLong()))
                achieve("catch")
            }

            +"breadCrumb"(P).fromPercept then {
                -"breadCrumb"(P).fromPercept
                +"found"(P)
            }

            +"agent"(N).fromPercept then {
                execute("stopAgent"(N))
                execute("print"("mandato messaggio"))
                -"state"("running").fromSelf
            }
            -achieve("catch") then {
                execute("print"("An error occurred during the implementation of the catch goal"))
            }
        }
    }
