package it.unibo.jakta.agents.bdi.messages

import it.unibo.tuprolog.core.Struct

data class Message(
    val from: String,
    val type: MessageType,
    val value: Struct,
)
