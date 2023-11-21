package it.unibo.jakta.agents.distributed.client.impl

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import it.unibo.jakta.agents.distributed.client.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.*

class WebSocketsClient(private val host: String, private val port: Int) : Client {
    override val incomingData: MutableMap<String, Any> = Collections.synchronizedMap(LinkedHashMap())
    private val publishSessions: MutableMap<String, DefaultClientWebSocketSession> =
        Collections.synchronizedMap(LinkedHashMap())
    private val subscribeSessions: MutableMap<String, DefaultClientWebSocketSession> =
        Collections.synchronizedMap(LinkedHashMap())
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    override suspend fun publish(topic: String, data: Any) {
        coroutineScope {
            launch(Dispatchers.Default) {
                if (!publishSessions.containsKey(topic)) {
                    publishSessions[topic] = client.webSocketSession(
                        host = host,
                        port = port,
                        path = "/publish/$topic",
                    )
                }
                try {
                    publishSessions[topic]?.sendSerialized(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun subscribe(topic: String) {
        coroutineScope {
            launch(Dispatchers.Default) {
                if (!subscribeSessions.containsKey(topic)) {
                    subscribeSessions[topic] = client.webSocketSession(
                        host = host,
                        port = port,
                        path = "/subscribe/$topic",
                    )
                }
                try {
                    for (frame in subscribeSessions[topic]?.incoming!!) {
                        incomingData[topic] = frame
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
