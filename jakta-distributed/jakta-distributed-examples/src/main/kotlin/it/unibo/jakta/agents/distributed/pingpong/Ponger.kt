@file:JvmName("Ponger")

package it.unibo.jakta.agents.distributed.pingpong

import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.distributed.broker.embedded.EmbeddedBroker
import it.unibo.jakta.agents.distributed.dsl.dmas
import it.unibo.jakta.agents.distributed.network.impl.WebsocketNetwork
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
                }

                +achieve("sendMessageTo"(M, R)) then {
                    iact("print"("Sending message ", M))
                    execute("send"(R, "tell", M))
                }
            }
        }
        network {
            WebsocketNetwork("localhost", 8080)
        }
        service {
            name("pinger")
        }
        embeddedBroker {
            EmbeddedBroker()
        }
    }.start()
}
