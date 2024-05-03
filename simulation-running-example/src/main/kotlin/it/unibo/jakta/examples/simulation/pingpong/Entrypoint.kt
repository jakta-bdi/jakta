@file:JvmName("Entrypoint")

package it.unibo.jakta.examples.simulation.pingpong

import it.unibo.alchemist.jakta.JaktaEnvironmentForAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.dsl.alchemistmas

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.pinger(): Agent =
    alchemistmas {
        agent("pinger") {
            beliefs {
                fact { "turn"("me") }
                fact { "other"("ponger") }
            }
            goals {
                achieve("send_ping")
            }
            plans {
                +achieve("send_ping") onlyIf {
                    "turn"("source"("self"), "me") and "other"("source"("self"), R)
                } then {
                    update("turn"("source"("self"), "other"))
                    achieve("sendMessageTo"("ball", R))
                }

                +"ball"("source"(R)) onlyIf {
                    "turn"("source"("self"), "other") and "other"("source"("self"), R)
                } then {
                    update("turn"("source"("self"), "me"))
                    iact("print"("Received ball from", R))
                    -"ball"("source"(R))
                    execute("print"("Pinger hasDone"))
                    execute("stop")
                }

                +achieve("sendMessageTo"(M, R)) then {
                    iact("print"("Sending message", M))
                    execute("sendTo"(R, "1", M))
                }
            }
        }
    }

fun <P : Position<P>> JaktaEnvironmentForAlchemist<P>.ponger(): Agent =
    alchemistmas {
        agent("ponger") {
            beliefs {
                fact { "turn"("other") }
                fact { "other"("pinger") }
            }
            plans {
                +"ball"("source"(S)) onlyIf {
                    "turn"("source"("self"), "other") and "other"("source"("self"), S)
                } then {
                    update("turn"("source"("self"), "me"))
                    -"ball"("source"(S))
                    achieve("sendMessageTo"("ball", S))
                    achieve("handle_ping")
                }

                +achieve("handle_ping") then {
                    update("turn"("source"("self"), "other"))
                    iact("print"("Ponger has Done"))
                    execute("stop")
                }

                +achieve("sendMessageTo"(M, R)) then {
                    iact("print"("Sending message", M))
                    execute("sendTo"(R, "0", M))
                }
            }
        }
    }
