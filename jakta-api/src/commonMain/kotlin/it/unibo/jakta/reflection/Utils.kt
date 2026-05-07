package it.unibo.jakta.reflection

import kotlin.reflect.KType

/**
 * Multi-platform implementation of KType.isSubtypeOf.
 */
expect fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean
