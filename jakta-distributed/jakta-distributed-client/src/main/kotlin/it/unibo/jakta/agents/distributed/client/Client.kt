package it.unibo.jakta.agents.distributed.client

import it.unibo.jakta.agents.distributed.client.impl.WebSocketsClient

interface Client {
    val incomingData: MutableMap<String, Any>
    suspend fun publish(topic: String, data: Any)
    suspend fun subscribe(topic: String)

    companion object {
        fun webSocketClient(host: String, port: Int): Client = WebSocketsClient(host, port)
    }
}
