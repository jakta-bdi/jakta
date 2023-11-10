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
import it.unibo.jakta.agents.distributed.broker.model.Error
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import it.unibo.jakta.agents.distributed.broker.model.UniqueID
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.*

const val PERIOD: Long = 15

fun Application.configureWebSockets(subscriptionManager: SubscriptionManager) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(PERIOD)
        timeout = Duration.ofSeconds(PERIOD)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    routing {

        val subscribersSessions: MutableMap<UniqueID, DefaultWebSocketSession> =
            Collections.synchronizedMap(LinkedHashMap())

        webSocket("/subscribe/{id}{topic}") {
            call.application.environment.log.info("New subscription: $this")
            val topic = UniqueID(call.parameters["topic"] ?: "")
            val id = UniqueID(call.parameters["id"] ?: "")
            subscriptionManager.addSubscriber(id, topic)
            subscribersSessions[id] = this
            for (frame in incoming) {
                this.send(Frame.Text(Error.BAD_REQUEST.toString()))
            }
            subscriptionManager.removeSubscriber(id, topic)
            subscribersSessions.remove(id)
        }

        webSocket("/publish/{topic}") {
            call.application.environment.log.info("New publish channel open: $this")
            val topic = UniqueID(call.parameters["topic"] ?: "")
            subscriptionManager.addTopic(topic)
            for (frame in incoming) {
                subscriptionManager.getSubscribers(topic)
                    .map { subscribersSessions[it] }
                    .forEach { it?.send(frame.copy()) }
            }
            call.application.environment.log.info("Removing $this")
            subscriptionManager.removeTopic(topic)
        }

        webSocket("/subscribe-all/{id}{except...}") {
            call.application.environment.log.info("New subscription: $this")
            val id = UniqueID(call.parameters["id"] ?: "")
            val except = call.parameters.getAll("except")?.map { UniqueID(it) } ?: emptyList()
            subscriptionManager.availableTopics().minus(except.toSet())
                .forEach { subscriptionManager.addSubscriber(id, it) }
            subscribersSessions[id] = this
            for (frame in incoming) {
                this.send(Frame.Text(Error.BAD_REQUEST.toString()))
            }
            subscriptionManager.availableTopics().minus(except.toSet())
                .forEach { subscriptionManager.removeSubscriber(id, it) }
            subscribersSessions.remove(id)
        }
    }
}
