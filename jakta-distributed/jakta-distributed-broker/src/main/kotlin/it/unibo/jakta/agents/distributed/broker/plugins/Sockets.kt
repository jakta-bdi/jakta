package it.unibo.jakta.agents.distributed.broker.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import java.time.Duration

const val PERIOD: Long = 15
fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(PERIOD)
        timeout = Duration.ofSeconds(PERIOD)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/echo") {
            send(Frame.Text("Please enter your name"))
            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                val receivedText = frame.readText()
                if (receivedText.equals("bye", ignoreCase = true)) {
                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                } else {
                    send(Frame.Text("Hi, $receivedText!"))
                }
            }
        }
    }
}
