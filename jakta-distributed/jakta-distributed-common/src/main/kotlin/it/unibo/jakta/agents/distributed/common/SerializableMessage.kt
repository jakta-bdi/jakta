package it.unibo.jakta.agents.distributed.common

import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.MessageType
import it.unibo.tuprolog.core.Struct
import kotlinx.serialization.Serializable

@Serializable
data class SerializableMessage(
    val from: String,
    val type: MessageType,
    val value: Struct,
) {
    companion object {
        fun fromMessage(message: Message): SerializableMessage {
            return SerializableMessage(message.from, message.type, message.value)
        }
    }
}
