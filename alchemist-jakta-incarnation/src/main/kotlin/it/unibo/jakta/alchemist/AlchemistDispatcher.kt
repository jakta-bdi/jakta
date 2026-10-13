package it.unibo.jakta.alchemist

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable

/**
 * Dispatcher that executes tasks immediately on the current thread and schedules delays and timeouts against
 * Alchemist simulated time, where a time unit is a second.
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
        schedule(timeMillis) { continuation.resume(Unit) }
    }

    /**
     * Fires the timeouts of `withTimeout` and `withTimeoutOrNull` in simulated time, on the simulation thread.
     * Without it, they would fall back to a wall-clock timer on another thread.
     */
    override fun invokeOnTimeout(timeMillis: Long, block: Runnable, context: CoroutineContext): DisposableHandle {
        val task = schedule(timeMillis, block)
        return DisposableHandle { queue.remove(task) }
    }

    private fun schedule(timeMillis: Long, action: Runnable): ScheduledTask {
        val now = alchemistEnvironment.simulation.time.toDouble()
        val task = ScheduledTask(simulatedTimeAfter(now, timeMillis), action)
        queue.add(task)
        return task
    }

    /**
     * Executes the pending jobs waiting in the jobs queue.
     */
    fun runDueTasks() {
        val now = alchemistEnvironment.simulation.time.toDouble()
        // println("Manage tasks until: $now")
        while (queue.isNotEmpty() && queue.peek().time <= now) {
            queue.poll().action.run()
        }
    }

    // not a data class: a cancelled timeout must remove its own task, not an equal one
    private class ScheduledTask(val time: Double, val action: Runnable)
}

/**
 * The simulated time, in seconds, [timeMillis] milliseconds after [now], without rounding to whole seconds.
 */
internal fun simulatedTimeAfter(now: Double, timeMillis: Long): Double = now + timeMillis / MILLIS_PER_SECOND

private const val MILLIS_PER_SECOND = 1000.0
