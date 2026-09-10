package it.unibo.jakta.reflection

import kotlin.reflect.KType

/**
 * Implements the JS behavior of checking if a KType is a subtype of another KType.
 * @see isSubtypeOfWithoutFullReflection
 */
actual fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean = isSubtypeOfWithoutFullReflection(other)
