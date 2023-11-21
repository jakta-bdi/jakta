package it.unibo.jakta.agents.distributed.common

import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import kotlinx.serialization.Serializable

@Serializable
data class SerializableSendMessage(
    val message: SerializableMessage,
    val recipient: String,
) {
    companion object {
        fun fromSendMessage(sendMessage: SendMessage): SerializableSendMessage {
            return SerializableSendMessage(SerializableMessage.fromMessage(sendMessage.message), sendMessage.recipient)
        }

        fun toSendMessage(serializableSendMessage: SerializableSendMessage): SendMessage {
            return SendMessage(
                SerializableMessage.toMessage(serializableSendMessage.message),
                serializableSendMessage.recipient
            )
        }
    }
}
