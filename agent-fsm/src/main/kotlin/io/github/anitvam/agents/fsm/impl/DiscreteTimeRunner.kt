package io.github.anitvam.agents.fsm.impl

import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.time.Time
import io.github.anitvam.agents.utils.Promise

class DiscreteTimeRunner(
    override val activity: Activity,
    private val currentTime: () -> Time
) : AbstractRunner(activity) {

    override fun onPause() = error("Is not possible to PAUSE a DiscreteTimeRunner")

    override fun onResume() = error("Is not possible to RESUME a DiscreteTimeRunner")

    override fun getCurrentTime(): Time = currentTime()

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
