package it.unibo.jakta.agents.fsm

import it.unibo.jakta.agents.fsm.impl.SimulatedTimeRunner
import it.unibo.jakta.agents.fsm.impl.State
import it.unibo.jakta.agents.fsm.impl.SyncRunner
import it.unibo.jakta.agents.fsm.impl.ThreadRunner
import it.unibo.jakta.agents.fsm.time.Time
import it.unibo.jakta.agents.utils.Promise

/**
 * A [Runner] defines a Final-State Machine (FSM) lifecycle logic.
 */
interface Runner {
    /**
     * The [Activity] executed during FSM execution.
     */
    val activity: Activity

    /**
     * The actual [State] the FSM machine is executing.
     */
    val state: State?

    /**
     * @return true if the FSM execution is ended, otherwise it returns false.
     */
    val isOver: Boolean

    /**
     * @return true if the FSM is in [State.PAUSED].
     */
    val isPaused: Boolean

    /**
     * Method that starts the FSM execution.
     *
     * @return a [Promise] that is completed when FSM execution is ended.
     */
    fun run(): Promise<Unit>

    companion object {
        fun syncOf(activity: Activity): Runner = SyncRunner(activity)

        fun threadOf(activity: Activity): Runner = ThreadRunner(activity)

        fun simulatedOf(
            activity: Activity,
            currentTime: () -> Time,
        ): Runner = SimulatedTimeRunner(activity, currentTime)
    }
}
