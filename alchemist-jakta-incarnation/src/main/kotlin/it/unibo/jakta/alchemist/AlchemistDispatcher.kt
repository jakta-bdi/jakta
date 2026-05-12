package it.unibo.jakta.alchemist

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration
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
        val targetTime = alchemistEnvironment.simulation.time.toDouble() + (timeMillis / 1000)
        queue.add(
            ScheduledTask(targetTime) {
                continuation.resume(Unit)
            }.also { println(it) },
        )
    }

    /**
     * Executes the pending jobs waiting in the jobs queue.
     */
    fun runDueTasks() {
        val now = alchemistEnvironment.simulation.time.toDouble()
        println("Manage tasks until: $now")
        while (queue.isNotEmpty() && queue.peek().time <= now) {
            queue.poll().action.run()
        }
    }

    private data class ScheduledTask(val time: Double, val action: Runnable)
}
