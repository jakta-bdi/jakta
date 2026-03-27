package it.unibo.jakta.agent

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

/**
 * Specifies how a [ExecutableAgent] can run.
 */
interface AgentLifecycle<Belief : Any, Goal : Any, Skills : Any> {

    /**
     * The [ExecutableAgent] that is currently executing.
     */
    val executableAgent: ExecutableAgent<Belief, Goal, Skills>

    /**
     * Runs a reasoning cycle step.
     * Suspends until the next [it.unibo.jakta.event.AgentEvent] is available and process it.
     * Potentially launches plans as a response to the event.
     * @param scope must be a SupervisorScope
     */
    suspend fun step()

    /**
     * Runs a reasoning cycle step if an event is available, otherwise does nothing.
     */
    fun tryStep(dispatcher: CoroutineDispatcher)
}
