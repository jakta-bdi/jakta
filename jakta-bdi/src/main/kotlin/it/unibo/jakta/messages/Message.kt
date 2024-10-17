package it.unibo.jakta.messages

import it.unibo.tuprolog.core.Struct

data class Message(
    val from: String,
    val type: MessageType,
    val value: Struct,
)
