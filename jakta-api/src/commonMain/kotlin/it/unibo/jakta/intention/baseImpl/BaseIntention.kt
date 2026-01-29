package it.unibo.jakta.intention.baseImpl

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.baseImpl.UnlimitedChannelBus
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.IntentionID
import kotlin.coroutines.CoroutineContext.Key
import kotlinx.coroutines.Job


data class BaseIntention(
    override val job: Job,
    override val id: IntentionID = BaseIntentionID(),
) : Intention {
    private val log =
        Logger(
            Logger.config,
            "Intention[${id.displayId}]",
        )

    private val _continuations: EventBus<() -> Unit> = UnlimitedChannelBus()

    override val continuations: EventInbox<() -> Unit>
        get() = _continuations

    override val key: Key<Intention> = Intention.Key

    val observers: MutableList<(Intention) -> Unit> = mutableListOf()

    override fun equals(other: Any?): Boolean = (other is Intention && id == other.id)

    override fun hashCode(): Int = id.hashCode()

    override suspend fun step() {
        _continuations.next().let {
            log.d { "Running one step" }
            it()
        }
    }

    override fun onReadyToStep(callback: (Intention) -> Unit) {
        observers.add(callback)
    }

    private fun notifyReadyToStep() {
        observers.forEach { it(this) }
    }

    override fun enqueue(continuation: () -> Unit) {
        log.d { "Resumed continuation and notify ready to step" }
        _continuations.send(continuation)
        notifyReadyToStep()
    }
}
