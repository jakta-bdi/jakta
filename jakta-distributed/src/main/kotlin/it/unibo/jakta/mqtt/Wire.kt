package it.unibo.jakta.mqtt

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.node.AcceptAll
import it.unibo.jakta.node.BroadcastFrom
import it.unibo.jakta.node.MessageFilter
import it.unibo.jakta.node.SendTo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * An agent message as it travels on the network.
 */
@Serializable
internal class Envelope(
    @Serializable(with = AgentIDSerializer::class) val sender: AgentID,
    @Polymorphic val filter: MessageFilter<*>,
    @Polymorphic val payload: Any,
)

/**
 * An [AgentID] as it travels on the network: [BaseAgentID]s are equal when their [id]s are,
 * and [AgentID.toString] is the id of a [BaseAgentID].
 */
@Serializable
private class WireAgentID(val id: String, val name: String)

/**
 * Serializes an [AgentID], to use in custom payloads and filters,
 * e.g. `@Serializable(with = AgentIDSerializer::class) val sender: AgentID`.
 */
object AgentIDSerializer : KSerializer<AgentID> by SurrogateSerializer(
    WireAgentID.serializer(),
    { WireAgentID(it.toString(), it.displayName) },
    { BaseAgentID(it.name, it.id) },
)

@Serializable
@SerialName("jakta.AcceptAll")
private data object AcceptAllWire

@Serializable
@SerialName("jakta.SendTo")
private class SendToWire(@Serializable(with = AgentIDSerializer::class) val receiver: AgentID)

@Serializable
@SerialName("jakta.BroadcastFrom")
private class BroadcastFromWire(@Serializable(with = AgentIDSerializer::class) val sender: AgentID)

/**
 * Serializes a [T] as its [surrogate], to keep jakta-api free of serialization.
 */
private class SurrogateSerializer<T, S>(
    private val surrogate: KSerializer<S>,
    private val toSurrogate: (T) -> S,
    private val fromSurrogate: (S) -> T,
) : KSerializer<T> {
    override val descriptor = surrogate.descriptor

    override fun serialize(encoder: Encoder, value: T) = encoder.encodeSerializableValue(surrogate, toSurrogate(value))

    override fun deserialize(decoder: Decoder): T = fromSurrogate(decoder.decodeSerializableValue(surrogate))
}

/**
 * Serializers always available to the [MqttNetwork]: [String] payloads and the built-in [MessageFilter]s.
 */
internal val builtinSerializers = SerializersModule {
    polymorphic(Any::class) {
        subclass(String::class, String.serializer())
    }
    polymorphic(MessageFilter::class) {
        subclass(AcceptAll::class, SurrogateSerializer(AcceptAllWire.serializer(), { AcceptAllWire }, { AcceptAll }))
        subclass(
            SendTo::class,
            SurrogateSerializer(SendToWire.serializer(), { SendToWire(it.receiver) }) { SendTo(it.receiver) },
        )
        subclass(
            BroadcastFrom::class,
            SurrogateSerializer(BroadcastFromWire.serializer(), { BroadcastFromWire(it.sender) }) {
                BroadcastFrom(it.sender)
            },
        )
    }
}
