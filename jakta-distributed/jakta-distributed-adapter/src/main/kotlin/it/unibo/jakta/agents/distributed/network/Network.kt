package it.unibo.jakta.agents.distributed.network

import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.utils.Promise

/** A network is a component that allows agents to communicate with each other.*/
interface Network {
    /**
     * Subscribes the DMas to the cluster, so that it can receive messages from other DMas.
     */
    fun subscribeToCluster(): Promise<Unit>
    /**
     * Sends a message with a specified recipient.
     */
    fun send(event: SendMessage)
    /**
     * Sends a message to all agents.
     */
    fun send(event: BroadcastMessage)
    /**
     * Returns all the messages received by the network as EnvironmentChange.
     * In this way a DMas can apply the effects of the messages to the environment by simply appending these changes to
     * the local ones.
     */
    fun getMessagesAsEnvironmentChanges(): Iterable<EnvironmentChange>
}
