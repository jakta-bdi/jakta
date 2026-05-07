package it.unibo.jakta.alchemist

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable

/**
 * Dispatcher that executes tasks immediately on the current thread and schedules delays against
 * Alchemist simulated time (expressed in milliseconds).
 */
@OptIn(InternalCoroutinesApi::class)
class AlchemistDispatcher<P : Position<P>>(private val alchemistEnvironment: Environment<Any?, P>) :
    CoroutineDispatcher(),
    Delay {

    private val queue = PriorityQueue<ScheduledTask>(compareBy { it.time })

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        block.run()
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val targetTime = alchemistEnvironment.simulation.time.toDouble() + timeMillis
        queue.add(
            ScheduledTask(targetTime) {
                continuation.resume(Unit)
            },
        )
    }

    /**
     * Executes the pending jobs waiting in the jobs queue.
     */
    fun runDueTasks() {
        val now = alchemistEnvironment.simulation.time.toDouble()
        while (queue.isNotEmpty() && queue.peek().time <= now) {
            queue.poll().action.run()
        }
    }

    private data class ScheduledTask(val time: Double, val action: Runnable)

    /**
     * Utilities for the Jakta incarnation custom dispatcher.
     */
    companion object {
        /**
         * The singleton dispatcher.
         * TODO("This needs to be changed. -> Each agent will have its own dispatcher")
         */
        var dispatcher: AlchemistDispatcher<*>? = null

        /**
         * Creates a singleton instance of the Jakta dispatcher.
         * @param alchemistEnvironment the alchemist Environment instance
         * @return the singleton instance of dispatcher.
         */
        @Suppress("UNCHECKED_CAST")
        fun <P : Position<P>> of(alchemistEnvironment: Environment<Any?, P>): AlchemistDispatcher<P> =
            dispatcher as? AlchemistDispatcher<P> ?: AlchemistDispatcher(alchemistEnvironment)
    }
}
