package it.unibo.jakta.agents.distributed.common

import it.unibo.jakta.agents.bdi.messages.Achieve
import it.unibo.jakta.agents.bdi.messages.Message
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.parsing.parse
import kotlinx.serialization.Serializable

@Serializable
data class SerializableMessage(
    val from: String,
    val type: String,
    val value: String,
) {
    companion object {
        fun fromMessage(message: Message): SerializableMessage {
            return SerializableMessage(message.from, message.type.toString(), message.value.toString())
        }

        fun toMessage(serializableMessage: SerializableMessage): Message {
            val type = when (serializableMessage.type) {
                Achieve.toString() -> Achieve
                Tell.toString() -> Tell
                else -> throw Exception()
            }
            return Message(serializableMessage.from, type, Struct.parse(serializableMessage.value))
        }
    }
}
