@file:JvmName("Pinger")

package it.unibo.jakta.agents.distributed.pingpong

import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.distributed.dsl.dmas
import it.unibo.jakta.agents.distributed.network.Network.Companion.websocketNetwork
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun main() {
    dmas {
        environment {
            actions {
                action("send", 3) {
                    val receiver: Atom = argument(0)
                    val type: Atom = argument(1)
                    val message: Struct = argument(2)
                    when (type.value) {
                        "tell" -> sendMessage(receiver.value, Message(this.sender, Tell, message))
                        "achieve" -> sendMessage(
                            receiver.value,
                            Message(this.sender, Achieve, message),
                        )
                    }
                }
            }
        }
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
                    iact("print"("Received ball from ", R))
                    -"ball"("source"(R))
                    iact("print"("Pinger hasDone"))
                }

                +achieve("sendMessageTo"(M, R)) then {
                    iact("print"("Sending message ", M))
                    execute("send"(R, "tell", M))
                }
            }
        }
        network {
            websocketNetwork("localhost", 8080)
        }
        service {
            name("ponger")
        }
    }.start()
}
