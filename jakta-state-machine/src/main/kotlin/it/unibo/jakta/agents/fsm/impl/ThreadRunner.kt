package it.unibo.jakta.agents.fsm.impl

import it.unibo.jakta.agents.fsm.Activity
import it.unibo.jakta.agents.fsm.time.Time
import it.unibo.jakta.agents.utils.Promise
import java.util.concurrent.Semaphore

/**
 * [AbstractRunner] implementation that executes the FSM on a separated thread.
 */
class ThreadRunner(
    override val activity: Activity,
) : AbstractRunner(activity) {
    private val promise = Promise<Unit>()
    private val mutex = Semaphore(0)
    private val thread =
        Thread {
            while (!isOver) {
                safeExecute({ promise.completeExceptionally(it) }) {
                    doStateTransition()
                }
            }
            promise.complete(null)
        }

    override fun onPause() = mutex.acquire()

    override fun onResume() = mutex.release()

    override fun getCurrentTime(): Time = Time.real(System.currentTimeMillis())

    override fun run(): Promise<Unit> {
        thread.start()
        return promise.thenApplyAsync {
            thread.join()
        }
    }
}
