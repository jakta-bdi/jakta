package io.github.anitvam.agents.fsm.impl

import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.Promise

/**
 * [AbstractRunner] implementation that executes the FSM on the current thread.
 */
class SyncRunner(override val activity: Activity) : AbstractRunner(activity) {

    override fun onPause() = error("Is not possible to PAUSE a SyncRunner")

    override fun onResume() = error("Is not possible to RESUME a SyncRunner")

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
