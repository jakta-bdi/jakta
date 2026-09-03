package it.unibo.jakta

/**
 * Represents methods intended to be used internally only.
 * The usage of these methods is discouraged and should be avoided.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class InternalJaktaAPI
