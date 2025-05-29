package it.unibo.jakta.fsm.impl

import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.fsm.Runner
import it.unibo.jakta.fsm.impl.AbstractRunner.Operation.CONTINUE
import it.unibo.jakta.fsm.impl.AbstractRunner.Operation.PAUSE
import it.unibo.jakta.fsm.impl.AbstractRunner.Operation.RESTART
import it.unibo.jakta.fsm.impl.AbstractRunner.Operation.STOP
import it.unibo.jakta.fsm.impl.State.CREATED
import it.unibo.jakta.fsm.impl.State.PAUSED
import it.unibo.jakta.fsm.impl.State.RUNNING
import it.unibo.jakta.fsm.impl.State.STARTED
import it.unibo.jakta.fsm.impl.State.STOPPED
import it.unibo.jakta.fsm.time.Time
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Abstract implementation of a FSM [Runner].
 */
abstract class AbstractRunner(override val activity: Activity) : Runner {
    /**
     * Operations that the FSM is going to perform in the next evaluation.
     */
    protected enum class Operation {
        PAUSE,
        STOP,
        CONTINUE,
        RESTART,
    }

    private var _state: State? = CREATED
    private var nextOperation: Operation = CONTINUE

    override val state get() = _state
    override val isOver get() = state == null
    override val isPaused get() = state == PAUSED

    private val controller =
        object : Activity.Controller {
            override fun stop() {
                nextOperation = STOP
                if (isPaused) onResume()
            }

            override fun currentTime(): Time = getCurrentTime()

            override fun sleep(millis: Long) {
                pause()
                // TODO("Executor Service to be removed")
                Executors
                    .newScheduledThreadPool(1)
                    .schedule({ resume() }, millis, TimeUnit.MILLISECONDS)
            }

            override fun restart() {
                nextOperation = RESTART
                if (isPaused) onResume()
            }

            override fun pause() {
                nextOperation = PAUSE
            }

            override fun resume() {
                if (isPaused) {
                    nextOperation = CONTINUE
                    _state = RUNNING
                    onResume()
                }
            }
        }

    /**
     * Method that evaluate the next state of the FSM and executes its callbacks.
     */
    protected fun doStateTransition() = when (_state) {
        CREATED -> doStateTransitionFromCreated(nextOperation)
        STARTED -> doStateTransitionFromStarted(nextOperation)
        RUNNING -> doStateTransitionFromRunning(nextOperation)
        PAUSED -> doStateTransitionFromPaused(nextOperation)
        STOPPED -> doStateTransitionFromStopped(nextOperation)
        else -> error("Reached illegal state: $_state")
    }

    /**
     * Method that handle the execution of an [action] that could trigger an Exception.
     *
     * @param onException action to perform in case the Exception is thrown
     * @param action the action to be performed safely
     */
    protected fun safeExecute(onException: (error: Throwable?) -> Unit, action: () -> Unit) {
        try {
            action()
        } catch (e: IllegalArgumentException) {
            _state = null
            onException(e)
        } catch (e: IllegalStateException) {
            _state = null
            onException(e)
        } catch (e: Exception) {
            onException(e)
            throw e
        }
    }

    private fun doStateTransitionFromCreated(whatToDo: Operation) = when (whatToDo) {
        CONTINUE -> {
            activity.onBegin(controller)
            _state = STARTED
        }
        else -> throw java.lang.IllegalArgumentException("Unexpected transition: $_state -$whatToDo-> ???")
    }

    private fun doStateTransitionFromStarted(whatToDo: Operation) = doStateTransitionFromRunning(whatToDo)

    private fun doStateTransitionFromPaused(whatToDo: Operation) = doStateTransitionFromRunning(whatToDo)

    private fun doStateTransitionFromRunning(whatToDo: Operation) = when (whatToDo) {
        PAUSE -> {
            _state = PAUSED
            onPause()
        }
        RESTART -> {
            _state = CREATED
            nextOperation = CONTINUE
        }
        STOP -> {
            activity.onEnd(controller)
            _state = STOPPED
        }
        CONTINUE -> {
            activity.onStep(controller)
            _state = RUNNING
        }
    }

    private fun doStateTransitionFromStopped(whatToDo: Operation) = when (whatToDo) {
        RESTART -> {
            _state = STARTED
            activity.onBegin(controller)
        }
        STOP, CONTINUE -> _state = null
        else -> throw IllegalArgumentException("Unexpected transition: $_state -$whatToDo-> ???")
    }

    /**
     * Runner's specific implementation actions to pause the FSM execution.
     */
    abstract fun onPause()

    /**
     * Runner's specific implementation actions to resume the FSM execution.
     */
    abstract fun onResume()

    abstract fun getCurrentTime(): Time
}
