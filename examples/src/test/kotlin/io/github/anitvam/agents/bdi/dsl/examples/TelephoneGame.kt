package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun main() {
    mas {
        environment {
            actions {
                action("send", 2) {
                    val receiver: Atom = argument(0)
                    val message: Struct = argument(1)
                    this.sendMessage(receiver.value, Message(this.sender, Tell, message))
                }
            }
        }
        for (i in 1..5) {
            agent("agent$i") {
                beliefs {
                    if (i == 5) {
                        fact("receiver"("agent1"))
                    } else {
                        fact("receiver"("agent${i + 1}"))
                    }
                }
                if (i == 1) {
                    goals {
                        achieve("start")
                    }
                }
                plans {
                    + achieve("start") iff {
                        "receiver"("source"("self"), R)
                    } then {
                        iact("print"("Starting..."))
                        act("send"(R, "msg"(1)))
                    }
                    + "msg"("source"(W), X) iff {
                        (X notEqualsTo 10) and (M `is` (X + 1)) and "receiver"("source"("self"), R)
                    } then {
                        iact("print"("Sending ", M))
                        act("send"(R, "msg"(M)))
                    }
                }
            }
        }
    }.start()
}
