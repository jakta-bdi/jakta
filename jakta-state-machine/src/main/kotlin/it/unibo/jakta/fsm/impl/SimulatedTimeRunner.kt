package it.unibo.jakta.fsm.impl

import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.time.Time
import it.unibo.jakta.utils.Promise

class SimulatedTimeRunner(override val activity: Activity, private val currentTime: () -> Time) :
    AbstractRunner(activity) {
    override fun onPause() = error("Is not possible to PAUSE a DiscreteTimeRunner")

    override fun onResume() = error("Is not possible to RESUME a DiscreteTimeRunner")

    // TODO("Sleep broken to be removed")

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
