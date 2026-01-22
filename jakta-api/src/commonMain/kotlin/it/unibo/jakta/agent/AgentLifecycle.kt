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
     * Runs a reasoning cycle step.
     * Suspends until the next [it.unibo.jakta.event.Event] is available and process it.
     * Potentially launches plans as a response to the event.
     * @param scope must be a SupervisorScope
     */
    //TODO can we remove the scope?
    //TODO we can definitely improve the documentation here
    suspend fun step(scope: CoroutineScope)

    /**
     * Stops the agent, cancelling the scheduling of its next iteration.
     */
    suspend fun stop()
}
