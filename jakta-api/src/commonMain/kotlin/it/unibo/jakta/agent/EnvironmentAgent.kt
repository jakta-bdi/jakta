package it.unibo.jakta.agent

import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventInbox

/**
 * The view of an [Agent] from the environment perspective.
 */
interface EnvironmentAgent<out Body: AgentBody> : Agent {

    /**
     * An inbox of [it.unibo.jakta.event.AgentEvent.External] that the [it.unibo.jakta.environment.Environment] can use
     * to send events to the agent.
     */
    val externalInbox : EventInbox<AgentEvent.External>

    /**
     * The [AgentBody] that represents the agent in the environment and can be visible from other agents.
     */
    val body: Body
}
