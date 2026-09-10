package it.unibo.jakta.reflection

import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun `same type is a subtype of itself`() {
        assertTrue(typeOf<Int>().isSubtypeOfWithoutFullReflection(typeOf<Int>()))
    }

    @Test
    fun `everything is a subtype of Any`() {
        assertTrue(typeOf<String>().isSubtypeOfWithoutFullReflection(typeOf<Any>()))
    }

    @Test
    fun `nullable is not a subtype of non-null Any`() {
        assertFalse(typeOf<String?>().isSubtypeOfWithoutFullReflection(typeOf<Any>()))
    }

    @Test
    fun `nullable is a subtype of nullable Any`() {
        assertTrue(typeOf<String?>().isSubtypeOfWithoutFullReflection(typeOf<Any?>()))
    }

    @Test
    fun `Nothing is a subtype of everything`() {
        val nothingType = typeOf<List<Nothing>>().arguments.single().type!!
        assertTrue(nothingType.isSubtypeOfWithoutFullReflection(typeOf<Int>()))
    }

    @Test
    fun `non-null is a subtype of its nullable counterpart`() {
        assertTrue(typeOf<Int>().isSubtypeOfWithoutFullReflection(typeOf<Int?>()))
    }

    @Test
    fun `nullable is not a subtype of its non-null counterpart`() {
        assertFalse(typeOf<Int?>().isSubtypeOfWithoutFullReflection(typeOf<Int>()))
    }

    @Test
    fun `unrelated classifiers are not related`() {
        assertFalse(typeOf<Int>().isSubtypeOfWithoutFullReflection(typeOf<String>()))
    }

    @Test
    fun `numeric types are subtypes of Number and Comparable`() {
        assertTrue(typeOf<Int>().isSubtypeOfWithoutFullReflection(typeOf<Number>()))
        assertTrue(typeOf<Double>().isSubtypeOfWithoutFullReflection(typeOf<Comparable<*>>()))
    }

    @Test
    fun `numeric types are not subtypes of each other`() {
        assertFalse(typeOf<Int>().isSubtypeOfWithoutFullReflection(typeOf<Long>()))
    }

    @Test
    fun `String is a subtype of CharSequence and Comparable`() {
        assertTrue(typeOf<String>().isSubtypeOfWithoutFullReflection(typeOf<CharSequence>()))
        assertTrue(typeOf<String>().isSubtypeOfWithoutFullReflection(typeOf<Comparable<*>>()))
    }

    @Test
    fun `collection interface hierarchy is respected with matching type arguments`() {
        assertTrue(typeOf<MutableList<Int>>().isSubtypeOfWithoutFullReflection(typeOf<List<Int>>()))
        assertTrue(typeOf<List<Int>>().isSubtypeOfWithoutFullReflection(typeOf<Collection<Int>>()))
        assertTrue(typeOf<Set<Int>>().isSubtypeOfWithoutFullReflection(typeOf<Iterable<Int>>()))
    }

    @Test
    fun `collection interface hierarchy is not accepted with mismatched type arguments`() {
        assertFalse(typeOf<List<Int>>().isSubtypeOfWithoutFullReflection(typeOf<Collection<String>>()))
    }

    @Test
    fun `unrelated collection interfaces are not related`() {
        assertFalse(typeOf<Set<Int>>().isSubtypeOfWithoutFullReflection(typeOf<List<Int>>()))
    }
}
