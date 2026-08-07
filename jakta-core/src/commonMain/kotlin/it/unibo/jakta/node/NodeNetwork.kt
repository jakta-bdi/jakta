package it.unibo.jakta.node

import it.unibo.jakta.event.SystemEvent

/**
 * Represents a network connecting nodes, allowing for communication and interaction with other nodes.
 */
interface NodeNetwork {

    /**
     * Starts the connection and returns a [NodeSubscription]
     * that allows for receiving [SystemEvent]s from the connection.
     */
    suspend fun subscribe(): NodeSubscription

    /**
     * Starts the connection without suspending and returns a [NodeSubscription]
     * that allows for receiving [SystemEvent]s from the connection, if possible.
     * If it is not possible to subscribe, the function returns null.
     */
    fun trySubscribe(): NodeSubscription?

    /**
     * Sends a [SystemEvent] through the network, allowing for communication and event propagation across nodes.
     */
    suspend fun send(event: SystemEvent)

    /**
     * Attempts to send an event to the network, without suspending.
     * If it fails, returns false.
     */
    fun trySend(event: SystemEvent): Boolean
}
