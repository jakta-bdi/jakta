package it.unibo.jakta.agents.bdi.dsl.examples.telephonegame

import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.count
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.print
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.receiver
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.self
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.send
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.source
import it.unibo.jakta.agents.bdi.dsl.examples.telephonegame.TelephoneProgramLiterals.start
import it.unibo.jakta.agents.bdi.dsl.mas
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun main() {
    val nAgents = 5
    val nMessages = 10
    mas {
        environment {
            actions {
                action(send, 2) {
                    val receiver: Atom = argument(0)
                    val message: Struct = argument(1)
                    this.sendMessage(receiver.value, Message(this.sender, Tell, message))
                }
            }
        }

        repeat(nAgents) {id ->
            agent("ag$id") {
                beliefs {
                    fact(receiver("ag${(id + 1) % nAgents}"))
                }
                if (id == 0) goals { achieve(start) }
                plans {
                    + achieve(start) onlyIf { receiver(source(self), R) } then {
                        execute(print("Starting..."))
                        execute(send(R, count(1)))
                    }
                    + count(source(`_`), X) onlyIf {
                        (X lowerThan nMessages) and (M `is` (X + 1)) and receiver(source(self), R)
                    } then {
                        execute(print("Sending ", M))
                        execute(send(R, count(M)))
                    }
                    + count(source(`_`), X) onlyIf { X greaterThanOrEqualsTo nMessages } then {
                        execute(print("Done!"))
                    }
                }
            }
        }
    }.start()
}

