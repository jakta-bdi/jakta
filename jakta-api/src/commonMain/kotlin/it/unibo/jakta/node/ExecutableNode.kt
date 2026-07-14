package it.unibo.jakta.node

import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent

/**
 * Represents a node that can be executed and managed, providing a connection to share system events.
 * @param Body The type of body used by agents in this node.
 */
interface ExecutableNode<Body : Any> : Node<Body> {

    /**
     * An event stream that emits system events related to the node.
     */
    val systemEvents: EventStream<SystemEvent>

    /**
     * Handles an external message received by the node,
     * allowing for processing and appropriate action based on the message content.
     */
    fun handleExternalMessage(event: SystemEvent.AgentMessage<*, *>)
}
