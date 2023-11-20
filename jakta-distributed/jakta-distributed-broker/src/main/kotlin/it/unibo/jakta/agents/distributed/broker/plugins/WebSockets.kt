package it.unibo.jakta.agents.distributed.broker.plugins

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import it.unibo.jakta.agents.distributed.broker.model.Error
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import kotlinx.serialization.json.Json
import java.time.Duration

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

        webSocket("/publish/{topic}") {
            call.application.environment.log.info("New publish channel open: $this")
            val topic = call.parameters["topic"] ?: ""
            subscriptionManager.addPublisher(this, topic)
            for (frame in incoming) {
                subscriptionManager.subscribers(topic)
                    .forEach { it.send(frame.copy()) }
            }
            call.application.environment.log.info("Removing $this")
            subscriptionManager.removePublisher(this, topic)
        }

        webSocket("/subscribe/{topic}") {
            call.application.environment.log.info("New subscription: $this")
            val topic = call.parameters["topic"] ?: ""
            subscriptionManager.addSubscriber(this, topic)
            for (frame in incoming) {
                this.send(Frame.Text(Error.BAD_REQUEST.toString()))
            }
            call.application.environment.log.info("Removing $this")
            subscriptionManager.removeSubscriber(this, topic)
        }

        webSocket("/subscribe-all/{except...}") {
            call.application.environment.log.info("New subscription: $this")
            val except = call.parameters.getAll("except") ?: emptyList()
            subscriptionManager.availableTopics().minus(except.toSet())
                .forEach { subscriptionManager.addSubscriber(this, it) }
            for (frame in incoming) {
                this.send(Frame.Text(Error.BAD_REQUEST.toString()))
            }
            call.application.environment.log.info("Removing $this")
            subscriptionManager.availableTopics().minus(except.toSet())
                .forEach { subscriptionManager.removeSubscriber(this, it) }
        }
    }
}
