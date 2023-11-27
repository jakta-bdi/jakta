package it.unibo.jakta.agents.distributed.client.impl

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.client.Client
import it.unibo.jakta.agents.distributed.common.SerializableBroadcastMessage
import it.unibo.jakta.agents.distributed.common.SerializableSendMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.*

class WebSocketsClient(private val host: String, private val port: Int) : Client {
    private val incomingData: MutableMap<String, SerializableSendMessage> = Collections.synchronizedMap(LinkedHashMap())
    private val publishSessions: MutableMap<String, DefaultClientWebSocketSession> =
        Collections.synchronizedMap(LinkedHashMap())
    private val subscribeSessions: MutableMap<String, DefaultClientWebSocketSession> =
        Collections.synchronizedMap(LinkedHashMap())
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
    }

    override suspend fun publish(topic: String, data: SendMessage) {
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
                    publishSessions[topic]?.sendSerialized(SerializableSendMessage.fromSendMessage(data))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun broadcast(data: BroadcastMessage) {
        coroutineScope {
            launch(Dispatchers.Default) {
                if (!publishSessions.containsKey("broadcast")) {
                    publishSessions["broadcast"] = client.webSocketSession(
                        host = host,
                        port = port,
                        path = "/publish/broadcast",
                    )
                }
                try {
                    publishSessions["broadcast"]?.sendSerialized(
                        SerializableBroadcastMessage.fromBroadcastMessage(data),
                    )
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
                    while (isActive) {
                        val message = subscribeSessions[topic]?.incoming?.receive()
                        message as? Frame.Text ?: continue
                        val receivedText = message.readText()
                        val x = checkDeserialization<SerializableSendMessage>(receivedText)
                        if (x != null) {
                            incomingData[topic] = x
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun incomingData(): Map<String, SendMessage> {
        return incomingData.mapValues { SerializableSendMessage.toSendMessage(it.value) }
    }

    private inline fun <reified T> checkDeserialization(string: String): T? {
        return try {
            Json.decodeFromString<T>(string)
        } catch (e: Exception) {
            null
        }
    }
}
