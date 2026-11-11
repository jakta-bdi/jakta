package it.unibo.jakta.reflection

import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Checks whether this type is a subtype of [other], giving **the same answer on every platform**.
 *
 * It is [isSubtypeOfWithoutFullReflection] everywhere, the JVM included: plan selection must not depend on
 * where the agent runs. The check is conservative: it never accepts a type that is not a subtype,
 * but it does not recognise the supertypes of user-defined classes. A plan returning a user-defined subtype
 * of the requested type has to declare the wider type (`returns<R>()` in the DSL).
 */
fun KType.isSubtypeOfMultiPlatform(other: KType): Boolean = isSubtypeOfWithoutFullReflection(other)

/**
 * Direct supertypes of the standard library classes whose hierarchy is fixed and well known,
 * so they can be hardcoded instead of requiring `kotlin-reflect-full` to discover them.
 */
private val knownDirectSupertypes: Map<KClass<*>, List<KClass<*>>> = mapOf(
    Byte::class to listOf(Number::class, Comparable::class),
    Short::class to listOf(Number::class, Comparable::class),
    Int::class to listOf(Number::class, Comparable::class),
    Long::class to listOf(Number::class, Comparable::class),
    Float::class to listOf(Number::class, Comparable::class),
    Double::class to listOf(Number::class, Comparable::class),
    Char::class to listOf(Comparable::class),
    Boolean::class to listOf(Comparable::class),
    String::class to listOf(CharSequence::class, Comparable::class),
    MutableList::class to listOf(List::class, MutableCollection::class),
    List::class to listOf(Collection::class),
    MutableSet::class to listOf(Set::class, MutableCollection::class),
    Set::class to listOf(Collection::class),
    MutableCollection::class to listOf(Collection::class),
    Collection::class to listOf(Iterable::class),
    MutableMap::class to listOf(Map::class),
)

private fun KClass<*>.isKnownSubclassOf(other: KClass<*>): Boolean =
    knownDirectSupertypes[this].orEmpty().any { it == other || it.isKnownSubclassOf(other) }

/**
 * Conservative subtype check that needs no `kotlin-reflect`, so it gives the same answers on every platform
 * (only the JVM could walk an arbitrary class hierarchy, with full reflection). Handles the universal cases
 * (`Any`, `Nothing`), same-classifier comparisons with correct nullability subsumption instead of
 * requiring an exact [KType] match, and the most obvious fixed stdlib relationships (numeric types
 * to `Number`/`Comparable`, `String` to `CharSequence`/`Comparable`, and the collection interface
 * chain `MutableList`/`List`/`MutableSet`/`Set`/`MutableCollection`/`Collection`/`Iterable`/`MutableMap`/`Map`).
 *
 * Generic arguments are accepted when equal, or when `other`'s are entirely star-projected
 * (e.g. `Comparable<*>`, `List<*>`) since a wildcard target matches regardless of `this`'s own
 * arguments (no variance/covariance modeling beyond that, e.g. `List<Int>` is recognized as a
 * `Collection<Int>` but not as a `Collection<Number>`). Supertype relationships of arbitrary
 * user-defined classes are NOT detected here, since [kotlin.reflect.KClass] alone exposes no
 * supertype information outside the JVM, and the JVM uses the same rule for consistency.
 * Such subtypes are never accepted, the same way everywhere: the check has false negatives, never false positives.
 */
fun KType.isSubtypeOfWithoutFullReflection(other: KType): Boolean {
    val nullabilityOk = !this.isMarkedNullable || other.isMarkedNullable
    return nullabilityOk &&
        when {
            other.classifier == Any::class -> true

            this.classifier == Nothing::class -> true

            else -> {
                val otherIsFullyStarProjected = other.arguments.all { it.type == null }
                if (!otherIsFullyStarProjected && this.arguments != other.arguments) {
                    false
                } else {
                    val thisClass = this.classifier as? KClass<*>
                    val otherClass = other.classifier as? KClass<*>
                    if (thisClass != null && otherClass != null) {
                        thisClass == otherClass || thisClass.isKnownSubclassOf(otherClass)
                    } else {
                        this.classifier == other.classifier
                    }
                }
            }
        }
}
