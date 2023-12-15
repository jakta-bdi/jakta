package it.unibo.jakta.agents.distributed.client.impl

import arrow.core.Either
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import it.unibo.jakta.agents.bdi.actions.effects.BroadcastMessage
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.SendMessage
import it.unibo.jakta.agents.distributed.client.Client
import it.unibo.jakta.agents.distributed.common.Error
import it.unibo.jakta.agents.distributed.common.SerializableBroadcastMessage
import it.unibo.jakta.agents.distributed.common.SerializableSendMessage
import it.unibo.tuprolog.utils.addFirst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.*

class WebSocketsClient(private val host: String, private val port: Int) : Client {
    private val incomingData: MutableMap<String, Either<SerializableSendMessage, SerializableBroadcastMessage>> =
        Collections.synchronizedMap(LinkedHashMap())
    private val publishSessions: MutableMap<String, DefaultClientWebSocketSession> =
        Collections.synchronizedMap(LinkedHashMap())
    private val subscribeSessions: MutableMap<String, DefaultClientWebSocketSession> =
        Collections.synchronizedMap(LinkedHashMap())
    private val disconnectedSessions: MutableList<String> = Collections.synchronizedList(LinkedList())
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
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
                        val serializedMessage = message.readText()
                        val deserializedMessage = tryDeserialization<SerializableSendMessage>(serializedMessage)
                            ?: tryDeserialization<SerializableBroadcastMessage>(serializedMessage)
                            ?: tryDeserialization<Error>(serializedMessage)
                        when (deserializedMessage) {
                            is SerializableSendMessage -> incomingData[topic] = Either.Left(deserializedMessage)
                            is SerializableBroadcastMessage -> incomingData[topic] = Either.Right(deserializedMessage)
                            Error.CLIENT_DISCONNECTED -> disconnectedSessions.addFirst(topic)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // connection is lost
                    disconnectedSessions.addFirst("broker")
                }
            }
        }
    }

    override fun incomingData(): Map<String, EnvironmentChange> {
        val list = incomingData.mapValues { entry ->
            when (val message = entry.value) {
                is Either.Right -> SerializableBroadcastMessage.toBroadcastMessage(message.value)
                is Either.Left -> SerializableSendMessage.toSendMessage(message.value)
            }
        }
        incomingData.clear()
        return list
    }

    override fun disconnections(): List<String> {
        val list = disconnectedSessions.toList()
        disconnectedSessions.clear()
        return list
    }

    private inline fun <reified T> tryDeserialization(string: String): T? {
        return try {
            Json.decodeFromString<T>(string)
        } catch (e: Exception) {
            null
        }
    }
}
