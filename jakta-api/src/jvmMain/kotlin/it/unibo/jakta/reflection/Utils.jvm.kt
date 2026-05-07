package it.unibo.jakta.reflection

import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf

/**
 * Implements the JVM behavior of checking if a KType is a subtype of another KType using Kotlin reflect full.
 */
actual fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean = this.isSubtypeOf(other)
