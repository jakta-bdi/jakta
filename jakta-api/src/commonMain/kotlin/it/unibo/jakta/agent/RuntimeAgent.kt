package it.unibo.jakta.agent

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventInbox

/**
 * The view of an [Agent] from the environment perspective.
 */
interface RuntimeAgent<out Body : Any> : Agent {

    /**
     * An inbox of [it.unibo.jakta.event.AgentEvent.External] that the [it.unibo.jakta.node.Node] can use
     * to send events to the agent.
     */
    val externalInbox: EventInbox<AgentEvent.External>

    /**
     * The [Body] that represents the agent in the environment and can be visible from other agents.
     */
    val body: Body
}
