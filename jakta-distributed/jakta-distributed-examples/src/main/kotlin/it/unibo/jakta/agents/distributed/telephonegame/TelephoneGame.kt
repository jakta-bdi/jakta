@file:JvmName("TelephoneGame")

package it.unibo.jakta.agents.distributed.telephonegame

import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.distributed.dsl.dmas
import it.unibo.jakta.agents.distributed.network.Network.Companion.websocketNetwork
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.count
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.print
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.receiver
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.self
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.send
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.source
import it.unibo.jakta.agents.distributed.telephonegame.TelephoneProgramLiterals.start
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun main(args: Array<String>) {
    val host: String = args[0]
    val port: Int = args[1].toInt()
    val id: Int = args[2].toInt()
    val nAgents = args[3].toInt()
    val nMessages = 5
    dmas {
        environment {
            actions {
                action(send, 2) {
                    val receiver: Atom = argument(0)
                    val message: Struct = argument(1)
                    this.sendMessage(receiver.value, Message(this.sender, Tell, message))
                }
            }
        }

        agent("ag$id") {
            beliefs {
                fact { receiver("ag${(id + 1) % nAgents}") }
            }
            if (id == 0) goals { achieve(start) }
            plans {
                +achieve(start) onlyIf { receiver(source(self), R) } then {
                    execute(print("Starting..."))
                    execute(send(R, count(1)))
                }
                +count(source(`_`), X) onlyIf {
                    (X lowerThan nMessages) and (M `is` (X + 1)) and receiver(source(self), R)
                } then {
                    execute(print("Sending ", M))
                    execute(send(R, count(M)))
                }
                +count(source(`_`), X) onlyIf { X greaterThanOrEqualsTo nMessages } then {
                    execute(print("Done!"))
                    execute("stop")
                }
            }
        }

        service {
            name("ag${(id + nAgents - 1) % nAgents}")
        }

        network {
            websocketNetwork(host, port)
        }
    }.start()
}
