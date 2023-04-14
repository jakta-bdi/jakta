package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.executionstrategies.ExecutionStrategy
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
                    println(
                        "[${this.sender}] is sending message from thread " +
                            "'${Thread.currentThread().name}' at time ${this.requestTimestamp}"
                    )
                    sendMessage(receiver.value, Message(this.sender, Tell, message))
                }
            }
        }
        agent("alice") {
            goals {
                achieve("send_message")
            }
            plans {
                + achieve("send_message") then {
                    execute("send"("bob", "message"))
                }
                + "message"("source"(S)) then {
                    iact("print"("Received message from ", S))
                }
            }
        }
        agent("bob") {
            plans {
                + "message"("source"(S)) then {
                    iact("print"("Received message from ", S))
                    execute("send"(S, "message"))
                }
            }
        }
        executionStrategy {
            ExecutionStrategy.discreteTimeExecution()
        }
    }.start()
}
