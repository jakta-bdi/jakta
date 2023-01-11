package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.messages.MessageType
import it.unibo.tuprolog.core.Struct

data class Message(
    val from: String,
    val type: MessageType,
    val value: Struct,
)
