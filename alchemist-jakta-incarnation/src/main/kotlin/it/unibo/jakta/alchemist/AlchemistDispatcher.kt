package it.unibo.jakta.alchemist

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 * Dispatcher that executes tasks immediately on the current thread and schedules delays against
 * Alchemist simulated time (expressed in milliseconds).
 */
@OptIn(InternalCoroutinesApi::class)
class AlchemistDispatcher<P: Position<P>>(
    private val alchemistEnvironment: Environment<Any?, P>,
) : CoroutineDispatcher(), Delay {

    private val queue = PriorityQueue<ScheduledTask>(compareBy { it.time })

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }

    override fun scheduleResumeAfterDelay(
        timeMillis: Long,
        continuation: CancellableContinuation<Unit>,
    ) {
        val targetTime = alchemistEnvironment.simulation.time.toDouble() + timeMillis
        queue.add(
            ScheduledTask(targetTime) {
                continuation.resume(Unit)
            }
        )
    }

    fun runDueTasks() {
        val now = alchemistEnvironment.simulation.time.toDouble()
        while (queue.isNotEmpty() && queue.peek().time <= now) {
            queue.poll().action.run()
        }
    }

    private data class ScheduledTask(
        val time: Double,
        val action: Runnable,
    )

    companion object {
        var dispatcher: AlchemistDispatcher<*>? = null
        @Suppress("UNCHECKED_CAST")
        fun <P: Position<P>> of(alchemistEnvironment: Environment<Any?, P>): AlchemistDispatcher<P> =
            dispatcher as? AlchemistDispatcher<P> ?: AlchemistDispatcher(alchemistEnvironment)
    }
}
