package it.unibo.jakta

import it.unibo.jakta.kqml.Achieve
import it.unibo.jakta.kqml.AskAll
import it.unibo.jakta.kqml.AskOne
import it.unibo.jakta.kqml.KQMLPayload
import it.unibo.jakta.kqml.Tell
import it.unibo.jakta.kqml.Unachieve
import it.unibo.jakta.kqml.Untell
import it.unibo.jakta.kqml.kqmlSerializersModule
import it.unibo.tuprolog.core.Atom
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Var
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.uuid.Uuid
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json

class TestKQMLSerialization {

    private val json = Json {
        useArrayPolymorphism = true
        serializersModule = kqmlSerializersModule
    }

    private fun roundTrip(payload: Any): Any = json.decodeFromString(
        PolymorphicSerializer(Any::class),
        json.encodeToString(PolymorphicSerializer(Any::class), payload),
    )

    private val ball = Struct.of("ball", Integer.of(1), Atom.of("hello world"))

    @Test
    fun `ground payloads survive the round trip with their id`() {
        listOf(Tell(setOf(Fact.of(ball)), Uuid.random()), Untell(ball), Achieve(ball), Unachieve(ball))
            .forEach { assertEquals<Any>(it, roundTrip(it)) }
    }

    @Test
    fun `queries survive the round trip with their id`() {
        val query = Struct.of("rounds", Var.of("X"))
        listOf(AskOne(query), AskAll(query)).forEach { payload ->
            val decoded = roundTrip(payload) as KQMLPayload
            assertEquals(payload::class, decoded::class)
            assertEquals(payload.id, decoded.id)
        }
    }

    @Test
    fun `told facts are decoded as facts`() {
        val decoded = roundTrip(Tell(setOf(Fact.of(ball)))) as Tell
        val belief = decoded.beliefs.single()
        assertIs<Fact>(belief)
        assertEquals(ball, belief.head)
    }

    @Test
    fun `variables keep their identity`() {
        val x = Var.of("X")
        val query = Struct.of("f", x, x, Var.of("X"))
        val decoded = (roundTrip(AskOne(query)) as AskOne).query
        assertEquals(decoded[0], decoded[1])
        assertNotEquals(decoded[0], decoded[2])
        assertEquals(2, decoded.variables.distinct().count())
    }
}
