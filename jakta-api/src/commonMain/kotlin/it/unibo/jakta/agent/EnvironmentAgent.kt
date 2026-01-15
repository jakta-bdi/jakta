package it.unibo.jakta.agent

import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventInbox

/**
 * The view of an [Agent] from the environment perspective.
 */
interface EnvironmentAgent<out Body: AgentBody> {

    /**
     * An inbox of [Event.External] that the [it.unibo.jakta.environment.Environment] can use
     * to send events to the agent.
     */
    val externalInbox : EventInbox<Event.External>

    /**
     * The [AgentBody] that represents the agent in the environment and can be visible from other agents.
     */
    val body: Body
}
