package it.unibo.jakta.agents.distributed.broker.plugins

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.origin
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import it.unibo.jakta.agents.distributed.broker.model.Error
import it.unibo.jakta.agents.distributed.broker.model.SubscriptionManager
import kotlinx.coroutines.channels.ClosedReceiveChannelException
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
            val topic = call.parameters["topic"] ?: ""
            call.application.environment.log.info(
                "${call.request.origin.remoteAddress}:${call.request.origin.remotePort} now publishes on $topic"
            )
            subscriptionManager.addPublisher(this, topic)
            try {
                for (frame in incoming) {
                    subscriptionManager.subscribers(topic)
                        .forEach { it.send(frame.copy()) }
                }
            } catch (e: ClosedReceiveChannelException) {
                call.application.environment.log.info("onClose ${closeReason.await()}")
            } catch (e: Throwable) {
                call.application.environment.log.error("onError ${closeReason.await()}")
                e.printStackTrace()
            } finally {
                call.application.environment.log.info(
                    "Removing ${call.request.origin.remoteAddress}:${call.request.origin.remotePort}"
                )
                subscriptionManager.removePublisher(this, topic)
            }
        }

        webSocket("/subscribe/{topic}") {
            val topic = call.parameters["topic"] ?: ""
            call.application.environment.log.info(
                "${call.request.origin.remoteAddress}:${call.request.origin.remotePort} subscribed on $topic"
            )
            subscriptionManager.addSubscriber(this, topic)
            try {
                for (frame in incoming) {
                    this.send(Frame.Text(Error.BAD_REQUEST.toString()))
                }
            } catch (e: ClosedReceiveChannelException) {
                call.application.environment.log.info("onClose ${closeReason.await()}")
            } catch (e: Throwable) {
                call.application.environment.log.error("onError ${closeReason.await()}")
                e.printStackTrace()
            } finally {
                call.application.environment.log.info(
                    "Removing ${call.request.origin.remoteAddress}:${call.request.origin.remotePort}"
                )
                subscriptionManager.removeSubscriber(this, topic)
            }
        }

        webSocket("/subscribe-all/{except...}") {
            call.application.environment.log.info(
                "New subscribe-all from ${call.request.origin.remoteAddress}:${call.request.origin.remotePort}"
            )
            val except = call.parameters.getAll("except") ?: emptyList()
            subscriptionManager.availableTopics().minus(except.toSet())
                .forEach { subscriptionManager.addSubscriber(this, it) }
            try {
                for (frame in incoming) {
                    this.send(Frame.Text(Error.BAD_REQUEST.toString()))
                }
            } catch (e: ClosedReceiveChannelException) {
                call.application.environment.log.info("onClose ${closeReason.await()}")
            } catch (e: Throwable) {
                call.application.environment.log.error("onError ${closeReason.await()}")
                e.printStackTrace()
            } finally {
                call.application.environment.log.info(
                    "Removing ${call.request.origin.remoteAddress}:${call.request.origin.remotePort}"
                )
                subscriptionManager.availableTopics().minus(except.toSet())
                    .forEach { subscriptionManager.removeSubscriber(this, it) }
            }
        }
    }
}
