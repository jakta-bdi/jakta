package it.unibo.jakta.agents.distributed.client

import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.client.impl.WebSocketsClient

interface Client {
    suspend fun publish(topic: String, data: SendMessage)

    suspend fun subscribe(topic: String)

    fun incomingData(): Map<String, SendMessage>

    companion object {
        fun webSocketClient(host: String, port: Int): Client = WebSocketsClient(host, port)
    }
}
