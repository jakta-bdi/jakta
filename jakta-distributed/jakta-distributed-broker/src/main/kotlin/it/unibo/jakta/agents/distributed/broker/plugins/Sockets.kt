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
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

const val PERIOD: Long = 15
fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(PERIOD)
        timeout = Duration.ofSeconds(PERIOD)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    routing {

        val subscriptions = Collections.synchronizedMap<String, Set<DefaultWebSocketSession>>(LinkedHashMap())

        webSocket("/topic/{topic}") {
            call.application.environment.log.info("New connection: $this")
            val thisSession = this
            val topic = call.parameters["topic"] ?: "all"
            if (subscriptions[topic].isNullOrEmpty()) subscriptions[topic] = LinkedHashSet()
            subscriptions[topic] = subscriptions[topic]?.plus(thisSession)
            try {
                for (frame in incoming) {
                    subscriptions[topic]
                        ?.filter { it != thisSession }
                        ?.forEach { it.send(frame.copy()) }
                }
            } catch (e: Exception) {
                call.application.environment.log.error(e.localizedMessage)
            } finally {
                call.application.environment.log.info("Removing $thisSession")
                subscriptions[topic]?.minus(thisSession)
            }
        }
    }
}
