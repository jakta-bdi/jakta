package it.unibo.jakta.fsm

import it.unibo.jakta.fsm.impl.ActivityImpl
import it.unibo.jakta.fsm.impl.State
import it.unibo.jakta.fsm.time.Time

/**
 * An [Activity] is something executed inside a final-state machine (FSM).
 */
interface Activity {
    /**
     * Set of actions performed in before [State.STARTED] and after [State.CREATED].
     *
     * @param controller [Controller] instance used to trigger an FSM's state change
     */
    fun onBegin(controller: Controller)

    /**
     * Set of actions performed in [State.RUNNING].
     *
     * @param controller [Controller] instance used to trigger an FSM's state change
     */
    fun onStep(controller: Controller)

    /**
     * Set of actions performed in [State.STOPPED].
     *
     * @param controller [Controller] instance used to trigger a FSM's state change
     */
    fun onEnd(controller: Controller)

    /**
     * Actions provided to trigger a change of state inside a final-state machine (FSM).
     */
    interface Controller {
        /**
         * Triggers a FSM state transition into [State.CREATED] when possible.
         */
        fun restart()

        /**
         * Triggers a FSM state transition into [State.PAUSED] when possible.
         */
        fun pause()

        /**
         * Triggers a FSM state transition into [State.RUNNING] from [State.PAUSED].
         */
        fun resume()

        /**
         * Triggers a FSM state transition into [State.STOPPED] when possible.
         */
        fun stop()

        fun currentTime(): Time

        fun sleep(millis: Long)
    }

    companion object {
        fun of(
            onBeginProcedure: (controller: Controller) -> Unit = {},
            onEndProcedure: (controller: Controller) -> Unit = {},
            onStepProcedure: (controller: Controller) -> Unit = {},
        ): Activity = ActivityImpl(onBeginProcedure, onStepProcedure, onEndProcedure)
    }
}
