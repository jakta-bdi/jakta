package it.unibo.jakta.agents.distributed.network

import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.RemoteService
import it.unibo.jakta.agents.distributed.network.impl.WebsocketNetwork

/** A network is a component that allows agents to communicate with each other.*/
interface Network {
    /**
     * Subscribes the DMas to a remote service.
     */
    suspend fun subscribe(remoteService: RemoteService)

    /**
     * Sends a message with a specified recipient.
     */
    suspend fun send(event: SendMessage)

    /**
     * Broadcasts a message to all the agents in the network.
     */
    suspend fun broadcast(event: BroadcastMessage)

    /**
     * Returns all the messages received by the network as EnvironmentChange.
     * In this way a DMas can apply the effects of the messages to the environment by simply appending these changes to
     * the local ones.
     */
    fun getMessagesAsEnvironmentChanges(): Iterable<EnvironmentChange>

    /**
     * Returns all the disconnections received by the network as RemoteService.
     * In this way a DMas can apply the effects of the disconnections to the environment by simply removing the
     * corresponding remote services.
     */
    fun getDisconnections(): Iterable<RemoteService>

    companion object {
        fun websocketNetwork(host: String, port: Int) = WebsocketNetwork(host, port)
    }
}
