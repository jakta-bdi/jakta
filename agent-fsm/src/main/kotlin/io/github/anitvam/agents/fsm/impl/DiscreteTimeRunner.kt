package io.github.anitvam.agents.fsm.impl

import io.github.anitvam.agents.fsm.Activity
import io.github.anitvam.agents.fsm.time.Time
import io.github.anitvam.agents.utils.Promise

class DiscreteTimeRunner(override val activity: Activity) : AbstractRunner(activity) {

    private var time = 0

    override fun onPause() = error("Is not possible to PAUSE a DiscreteTimeRunner")

    override fun onResume() = error("Is not possible to RESUME a DiscreteTimeRunner")

    override fun getCurrentTime(): Time = Time.of(time)

    override fun run(): Promise<Unit> {
        val promise = Promise<Unit>()
        while (!isOver) {
            safeExecute({ promise.completeExceptionally(it) }) {
                doStateTransition()
            }
            time++
        }
        promise.complete(null)
        return promise
    }
}
