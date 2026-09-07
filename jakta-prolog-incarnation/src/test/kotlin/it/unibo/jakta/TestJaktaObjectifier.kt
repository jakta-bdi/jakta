package it.unibo.jakta

import it.unibo.jakta.dsl.belief.initialBelief
import it.unibo.jakta.dsl.belief.matchingBelief
import it.unibo.jakta.logic.JaktaLogicProgrammingScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestJaktaObjectifier {

    @Test
    fun `integer to int`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(1, X.value<Int>()) }
        }
    }

    @Test
    fun `integer to long`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(1L, X.value<Long>()) }
        }
    }

    @Test
    fun `integer to double`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(1.0, X.value<Double>()) }
        }
    }

    @Test
    fun `integer to float`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(1f, X.value<Float>()) }
        }
    }

    @Test
    fun `integer to string`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals("1", X.value<String>()) }
        }
    }

    @Test
    fun `integer to short`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(1.toShort(), X.value<Short>()) }
        }
    }

    @Test
    fun `integer to byte`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "num"(1) }
            val ctx = belief.matchingBelief { "num"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(1.toByte(), X.value<Byte>()) }
        }
    }

    @Test
    fun `real to double`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "r"(2.5) }
            val ctx = belief.matchingBelief { "r"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(2.5, X.value<Double>()) }
        }
    }

    @Test
    fun `real to float`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "r"(2.5) }
            val ctx = belief.matchingBelief { "r"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals(2.5f, X.value<Float>()) }
        }
    }

    @Test
    fun `real to string`() {
        with(JaktaLogicProgrammingScope()) {
            val belief = initialBelief { "r"(2.5) }
            val ctx = belief.matchingBelief { "r"(X) }
            assertNotNull(ctx)
            with(ctx) { assertEquals("2.5", X.value<String>()) }
        }
    }

    @Test
    fun `string atom extraction`() {
        with(JaktaLogicProgrammingScope()) {
            val sBel = initialBelief { "s"("hello") }
            val sCtx = sBel.matchingBelief { "s"(X) }
            assertNotNull(sCtx)
            with(sCtx) { assertEquals("hello", X.value<String>()) }
        }
    }

    @Test
    fun `boolean truth extraction`() {
        with(JaktaLogicProgrammingScope()) {
            val bBel = initialBelief { "b"(true) }
            val bCtx = bBel.matchingBelief { "b"(X) }
            assertNotNull(bCtx)
            with(bCtx) { assertTrue(X.value<Boolean>()) }
        }
    }

    @Test
    fun `list extraction`() {
        with(JaktaLogicProgrammingScope()) {
            val lBel = initialBelief { "l"(logicList(1, 2)) }
            val lCtx = lBel.matchingBelief { "l"(X) }
            assertNotNull(lCtx)
            with(lCtx) {
                val lst = X.value<List<Double>>()
                assertEquals(2, lst.size)
                assertEquals(1.0, lst[0])
                assertEquals(2.0, lst[1])
            }
        }
    }

    @Test
    fun `empty list extraction`() {
        with(JaktaLogicProgrammingScope()) {
            val eBel = initialBelief { "e"(logicList()) }
            val eCtx = eBel.matchingBelief { "e"(X) }
            assertNotNull(eCtx)
            with(eCtx) { assertTrue(X.value<List<Int>>().isEmpty()) }
        }
    }

    @Test
    fun `tuple extraction`() {
        with(JaktaLogicProgrammingScope()) {
            val tBel = initialBelief { "t"(tupleOf(1, "hello")) }
            val tCtx = tBel.matchingBelief { "t"(X) }
            assertNotNull(tCtx)
            with(tCtx) {
                val p = X.value<Pair<Int, String>>()
                assertEquals(1, p.first)
                assertEquals("hello", p.second)
            }
        }
    }
}
