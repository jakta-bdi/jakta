package it.unibo.jakta.agents.distributed.common

import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import kotlinx.serialization.Serializable

@Serializable
class SerializableBroadcastMessage(val serializableMessage: SerializableMessage) {
    companion object {
        fun fromBroadcastMessage(broadcastMessage: BroadcastMessage): SerializableBroadcastMessage {
            return SerializableBroadcastMessage(SerializableMessage.fromMessage(broadcastMessage.message))
        }

        fun toBroadcastMessage(serializableBroadcastMessage: SerializableBroadcastMessage): BroadcastMessage {
            return BroadcastMessage(SerializableMessage.toMessage(serializableBroadcastMessage.serializableMessage))
        }
    }
}
