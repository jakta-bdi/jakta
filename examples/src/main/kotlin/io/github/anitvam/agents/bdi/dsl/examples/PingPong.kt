package io.github.anitvam.agents.bdi.dsl.examples

import io.github.anitvam.agents.bdi.Message
import io.github.anitvam.agents.bdi.dsl.mas
import io.github.anitvam.agents.bdi.messages.Achieve
import io.github.anitvam.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Struct

fun main() {
    mas {
        environment {
            actions {
                action("send", 2) {
                    val receiver: Atom = argument(0)
                    val type: Atom = argument(1)
                    val message: Struct = argument(2)
                    when (type.value) {
                        "tell" -> sendMessage(receiver.value, Message(this.sender, Tell, message))
                        "achieve" -> sendMessage(
                            receiver.value,
                            Message(this.sender, Achieve, message)
                        )
                    }
                }
            }
        }
    }
}
