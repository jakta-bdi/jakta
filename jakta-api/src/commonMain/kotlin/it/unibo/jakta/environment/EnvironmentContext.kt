package it.unibo.jakta.environment

import kotlin.coroutines.CoroutineContext

// TODO maybe support the typed key rather than the top level interface?
// What if we made Environment inherit from CoroutineContext.Element directly?

/**
 * Coroutine context element to hold the [Environment].
 * @param[environment] the environment to hold in the context.
 */
class EnvironmentContext(val environment: Environment) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    /**
     * Key for [EnvironmentContext] in a [CoroutineContext].
     */
    companion object Key : CoroutineContext.Key<EnvironmentContext>
}
