package it.unibo.jakta.agents.distributed.broker.plugins

import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.testing.testApplication
import it.unibo.jakta.agents.distributed.broker.module
import kotlin.test.Test

class SocketsTest {

    @Test
    fun testWebsocketEcho() = testApplication {
        application {
            module()
        }
        val client = createClient {
            install(WebSockets)
        }
        client.webSocket("/echo") {
            TODO("Please write your test here")
        }
    }
}
