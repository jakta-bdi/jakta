package it.unibo.jakta.intention

import co.touchlab.kermit.Logger
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.job

/**
 * A custom dispatcher for intentions, that enqueues the continuation of an intention instead of dispatching it.
 * Continuations are enqueued even when the intention is cancelled: running them on the intention's next steps
 * lets its plans complete their cancellation (e.g. run their finally blocks) sequentially with the agent.
 */
open class IntentionDispatcher protected constructor() : CoroutineDispatcher() {

    private val log =
        Logger(
            Logger.config,
            "Interceptor",
        )

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        log.d { "Intercepting continuation with context: $context" }
        val currentIntention: Intention = context[Intention] as Intention
        currentIntention.enqueue { block.run() }
    }

    /** Factory for [IntentionDispatcher]s. */
    companion object {
        /**
         * Creates an [IntentionDispatcher] for plans launched on [interceptor].
         * If [interceptor] is a [Delay] (e.g. a `TestDispatcher` with virtual time, or a simulated clock),
         * delays are scheduled on it. Otherwise `delay` falls back to the kotlinx.coroutines default, as it does
         * for any other non-[Delay] dispatcher (e.g. `Dispatchers.Default` and `Dispatchers.IO` on JVM and native).
         */
        @OptIn(InternalCoroutinesApi::class)
        operator fun invoke(interceptor: ContinuationInterceptor): IntentionDispatcher =
            if (interceptor is Delay) DelayingIntentionDispatcher(interceptor) else IntentionDispatcher()
    }
}

// Pattern from https://github.com/Kotlin/kotlinx.coroutines/issues/3758#issuecomment-3059351061
// Delay is @InternalCoroutinesApi: kotlinx.coroutines offers no public way to propagate it.
@OptIn(InternalCoroutinesApi::class)
private class DelayingIntentionDispatcher(delay: Delay) :
    IntentionDispatcher(),
    Delay by delay
