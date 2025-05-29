package it.unibo.jakta.fsm.impl

import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.utils.Promise

/**
 * [AbstractRunner] implementation that executes the FSM on the current thread.
 */
class SyncRunner(override val activity: Activity) : AbstractRunner(activity) {
    override fun onPause() = error("Is not possible to PAUSE a SyncRunner")

    override fun onResume() = error("Is not possible to RESUME a SyncRunner")

    override fun getCurrentTime(): Time = Time.real(System.currentTimeMillis())

    override fun run(): Promise<Unit> {
        val promise = Promise<Unit>()
        while (!isOver) {
            safeExecute({ promise.completeExceptionally(it) }) {
                doStateTransition()
            }
        }
        promise.complete(null)
        return promise
    }
}
