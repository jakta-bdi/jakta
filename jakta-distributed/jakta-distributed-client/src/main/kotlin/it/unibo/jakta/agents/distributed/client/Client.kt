package it.unibo.jakta.agents.distributed.client

import it.unibo.jakta.agents.distributed.client.impl.WebSocketsClient

interface Client {
    fun publish(topic: String, data: Any)
    fun subscribe(topic: String)
    val incomingData: MutableMap<String, Any>

    companion object {
        fun webSocketClient(host: String, port: Int): Client = WebSocketsClient(host, port)
    }
}
