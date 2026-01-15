package it.unibo.jakta.agent

import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventReceiver

/**
 * Agent information visible from the environment.
 */
interface EnvironmentAgent<out Body: AgentBody> {
    /**
     * [Event.External] that must be managed by the agent.
     */
    val inbox : EventReceiver

    /**
     * The [AgentBody].
     */
    val body: Body
}
