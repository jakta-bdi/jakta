package it.unibo.jakta.agents.distributed.client.impl

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import it.unibo.jakta.agents.distributed.client.Client
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.*

class WebSocketsClient : Client {
    override val incomingData: MutableMap<String, Any> = Collections.synchronizedMap(LinkedHashMap())
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    override fun publish(topic: String, data: Any) {
        runBlocking {
            client.webSocket(
                path = "/publish/$topic",
            ) {
                sendSerialized(data)
            }
        }
    }

    override fun subscribe(topic: String) {
        runBlocking {
            client.webSocket(
                path = "/subscribe/$topic",
            ) {
                for (frame in incoming) {
                    incomingData[topic] = frame
                }
            }
        }
    }
}
