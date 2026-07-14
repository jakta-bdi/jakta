package it.unibo.jakta.node

import it.unibo.jakta.event.SystemEvent

/**
 * Represents a connection within a node, allowing for communication and interaction with other nodes.
 */
interface NodeConnection {

    /**
     * Starts the connection and returns a [NodeSubscription]
     * that allows for receiving [SystemEvent]s from the connection.
     */
    suspend fun subscribe(): NodeSubscription

    /**
     * Sends a [SystemEvent] through the connection, allowing for communication and event propagation across nodes.
     */
    suspend fun send(event: SystemEvent)
}
