package it.unibo.jakta.agents.distributed.network

import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.network.impl.WebsocketNetwork

/** A network is a component that allows agents to communicate with each other.*/
interface Network {
    /**
     * Sends a message with a specified recipient.
     */
    fun send(event: SendMessage)
    /**
     * Returns all the messages received by the network as EnvironmentChange.
     * In this way a DMas can apply the effects of the messages to the environment by simply appending these changes to
     * the local ones.
     */
    fun getMessagesAsEnvironmentChanges(): Iterable<EnvironmentChange>

    companion object {
        fun websocketNetwork() = WebsocketNetwork()
    }
}
