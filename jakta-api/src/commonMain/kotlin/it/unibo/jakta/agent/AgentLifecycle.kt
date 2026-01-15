package it.unibo.jakta.agent

import kotlinx.coroutines.CoroutineScope

/**
 * Specifies how a [RunnableAgent] can run.
 */
interface AgentLifecycle<Belief: Any, Goal: Any, Skills: Any> {

    /**
     * The [RunnableAgent] that is currently executing.
     */
    val runnableAgent: RunnableAgent<Belief, Goal, Skills>

    /**
     * Non-blocking run of a reasoning cycle step, i.e. it suspends until the next event is available and process it.
     * Potentially launches plans as a response to the event.
     * @param scope must be a SupervisorScope
     */
    suspend fun step(scope: CoroutineScope)

    /**
     * Non-blocking stop of the agent, cancelling the scheduling of its next iteration.
     */
    suspend fun stop()
}
