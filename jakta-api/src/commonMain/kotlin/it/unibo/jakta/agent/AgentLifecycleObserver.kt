package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent

/**
 * Observes the reasoning cycle of agents, for instance to inspect or debug them.
 *
 * An [AgentLifecycle] calls it in the agent's own loop, right after the agent handled an [AgentEvent]:
 * implementations must be quick and must not change the agent (read its state, copy what they need, and return).
 */
fun interface AgentLifecycleObserver {

    /**
     * Called after [agent] handled [event].
     */
    fun onEventHandled(agent: ExecutableAgent<*, *>, event: AgentEvent)

    /** The observers available by default. */
    companion object {
        /**
         * An observer that does nothing.
         */
        val None: AgentLifecycleObserver = AgentLifecycleObserver { _, _ -> }
    }
}
