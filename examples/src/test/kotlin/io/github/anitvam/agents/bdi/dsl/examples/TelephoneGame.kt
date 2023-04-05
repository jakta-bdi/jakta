package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun main() {
    val nAgents = 5
    val nMessages = 10
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
        for (i in 1..nAgents) {
            agent("ag$i") {
                beliefs {
                    if (i == nAgents) {
                        fact("receiver"("ag1"))
                    } else {
                        fact("receiver"("ag${i + 1}"))
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
                        act("send"(R, "count"(1)))
                    }
                    + "count"("source"(`_`), X) iff {
                        (X lowerThan nMessages) and (M `is` (X + 1)) and "receiver"("source"("self"), R)
                    } then {
                        iact("print"("Sending ", M))
                        act("send"(R, "count"(M)))
                    }
                    + "count"("source"(`_`), X) iff {
                        X greaterThanOrEqualsTo nMessages
                    } then {
                        iact("print"("Done!"))
                    }
                }
            }
        }
    }.start()
}
