package it.unibo.jakta.node

import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent

/**
 * Represents a subscription to a node connection.
 */
interface NodeSubscription {

    /**
     * The event queue associated with this subscription, which receives [SystemEvent]s from the node connection.
     */
    val queue: EventQueue<SystemEvent>

    /**
     * Closes the subscription, stopping the reception of events and cleaning up resources.
     */
    suspend fun close()
}
