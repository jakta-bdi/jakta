package io.github.anitvam.agents.fsm.impl

import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.time.Time
import io.github.anitvam.agents.utils.Promise
import java.util.concurrent.Semaphore

/**
 * [AbstractRunner] implementation that executes the FSM on a separated thread.
 */
class ThreadRunner(override val activity: Activity) : AbstractRunner(activity) {
    private val promise = Promise<Unit>()
    private val mutex = Semaphore(0)
    private val thread = Thread {
        while (!isOver) {
            safeExecute({ promise.completeExceptionally(it) }) {
                doStateTransition()
            }
        }
        promise.complete(null)
    }

    override fun onPause() = mutex.acquire()
    override fun onResume() = mutex.release()

    override fun getCurrentTime(): Time = Time.of(System.currentTimeMillis())

    override fun run(): Promise<Unit> {
        thread.start()
        return promise.thenApplyAsync {
            thread.join()
        }
    }
}
