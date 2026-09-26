package it.unibo.jakta.kqml

import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.parsing.parse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// Complete variable names (e.g. X_12) keep distinct variables with the same name distinct once parsed back.
// ponytail: annotations are term tags, which are neither formatted nor parsed, so they are lost on the wire
//  (the receiver re-tags with source(sender)), see https://github.com/jakta-bdi/jakta/issues/960
private val formatter = TermFormatter.of(TermFormatter.VarFormat.COMPLETE_NAME, TermFormatter.OpFormat.COLLECTIONS)

/**
 * Serializes a tuProlog [Struct] as its textual Prolog representation.
 */
object StructSerializer : KSerializer<Struct> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("it.unibo.tuprolog.core.Struct", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Struct) = encoder.encodeString(formatter.format(value))

    override fun deserialize(decoder: Decoder): Struct = Struct.parse(decoder.decodeString())
}

/**
 * Serializes a tuProlog [Rule] (and thus a Fact) as its textual Prolog representation.
 */
object RuleSerializer : KSerializer<Rule> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("it.unibo.tuprolog.core.Rule", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Rule) = encoder.encodeString(formatter.format(value))

    override fun deserialize(decoder: Decoder): Rule = Rule.parse(decoder.decodeString())
}

/**
 * Registers the [KQMLPayload]s as polymorphic message payloads, to send them over a network.
 */
val kqmlSerializersModule = SerializersModule {
    polymorphic(Any::class) {
        subclass(Tell::class, Tell.serializer())
        subclass(Untell::class, Untell.serializer())
        subclass(Achieve::class, Achieve.serializer())
        subclass(Unachieve::class, Unachieve.serializer())
        subclass(AskOne::class, AskOne.serializer())
        subclass(AskAll::class, AskAll.serializer())
    }
}
