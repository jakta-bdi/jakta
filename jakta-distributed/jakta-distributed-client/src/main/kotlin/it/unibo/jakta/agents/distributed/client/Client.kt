package it.unibo.jakta.agents.distributed.client

import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.client.impl.WebSocketsClient

interface Client {
    suspend fun publish(topic: String, data: SendMessage)

    suspend fun subscribe(topic: String)

    suspend fun broadcast(data: BroadcastMessage)

    fun incomingData(): Map<String, EnvironmentChange>

    companion object {
        fun webSocketClient(host: String, port: Int): Client = WebSocketsClient(host, port)
    }
}
