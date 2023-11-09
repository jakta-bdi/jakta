package it.unibo.jakta.agents.distributed.broker.plugins

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

const val PERIOD: Long = 15

fun Application.configureWebSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(PERIOD)
        timeout = Duration.ofSeconds(PERIOD)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    routing {

        val subscribers = Collections.synchronizedMap<String, Set<DefaultWebSocketSession>>(LinkedHashMap())
        val publishers = Collections.synchronizedMap<String, Set<DefaultWebSocketSession>>(LinkedHashMap())

        webSocket("/subscribe/{topic}") {
            call.application.environment.log.info("New connection: $this")
            val topic = call.parameters["topic"] ?: "all"
            if (subscribers[topic].isNullOrEmpty()) subscribers[topic] = LinkedHashSet()
            subscribers[topic] = subscribers[topic]?.plus(this)
            try {
                for (frame in incoming) {
                    this.send(Frame.Text("400"))
                }
            } catch (e: ClosedReceiveChannelException) {
                call.application.environment.log.info("onClose ${closeReason.await()}")
            } catch (e: Throwable) {
                call.application.environment.log.error("onError ${closeReason.await()}")
                call.application.environment.log.error(e.printStackTrace().toString())
            } finally {
                call.application.environment.log.info("Removing $this")
                subscribers[topic]?.minus(this)
            }
        }

        webSocket("/publish/{topic}") {
            call.application.environment.log.info("New connection: $this")
            val topic = call.parameters["topic"] ?: "all"
            if (publishers[topic].isNullOrEmpty()) publishers[topic] = LinkedHashSet()
            publishers[topic] = publishers[topic]?.plus(this)
            try {
                for (frame in incoming) {
                    subscribers[topic]
                        ?.forEach { it.send(frame.copy()) }
                }
            } catch (e: ClosedReceiveChannelException) {
                call.application.environment.log.info("onClose ${closeReason.await()}")
            } catch (e: Throwable) {
                call.application.environment.log.error("onError ${closeReason.await()}")
                call.application.environment.log.error(e.printStackTrace().toString())
            } finally {
                call.application.environment.log.info("Removing $this")
                publishers[topic]?.minus(this)
            }
        }
    }
}
