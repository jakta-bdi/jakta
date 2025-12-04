package it.unibo.jakta.reflection

import kotlin.reflect.KClass
import kotlin.reflect.KType

// TODO check if a better subtype check is possible
/**
 * Implements the JS behavior of checking if a KType is a subtype of another KType.
 */
actual fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean {
    if (this == other) {
        return true
    }

    val otherClass = other.classifier as? KClass<*>
    return otherClass == Any::class
}
