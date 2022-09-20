package bidiai

import bidiai.impl.State

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
}
