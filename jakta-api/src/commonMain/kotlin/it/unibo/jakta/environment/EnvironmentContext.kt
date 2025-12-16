package it.unibo.jakta.environment

import it.unibo.jakta.event.Event
import kotlin.coroutines.CoroutineContext

// TODO maybe support the typed key rather than the top level interface?
// What if we made Environment inherit from CoroutineContext.Element directly?

/**
 * Coroutine context element to hold the [Environment].
 * @param[environment] the environment to hold in the context.
 */
//class EnvironmentContext<Belief : Any, Goal : Any>(val environment: Environment<Belief, Goal>) :
//    CoroutineContext.Element {
//    override val key: CoroutineContext.Key<*> = Key
//
//    /**
//     * Key for [EnvironmentContext] in a [CoroutineContext].
//     */
//    companion object Key : CoroutineContext.Key<EnvironmentContext<*, *>> // TODO: I'm losing the generics here
//
//}
