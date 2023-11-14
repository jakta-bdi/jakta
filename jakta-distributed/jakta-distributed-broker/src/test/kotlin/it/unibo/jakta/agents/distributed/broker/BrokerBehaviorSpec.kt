package it.unibo.jakta.agents.distributed.broker

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import it.unibo.jakta.agents.distributed.broker.model.Topic
import org.junit.Test
import kotlin.test.assertEquals

class BrokerBehaviorSpec {
    @Test
    fun testTopicPublication() {
        testApplication {
            val mas0 = createClient {
                install(ContentNegotiation) {
                    json()
                }
                install(WebSockets)
            }
            val mas1 = createClient {
                install(ContentNegotiation) {
                    json()
                }
                install(WebSockets)
            }

            val topic1 = "topic1"
            val topic2 = "topic2"

            mas0.webSocket("/publish/$topic1") {
                send(Frame.Text("message0"))
            }

            mas1.webSocket("/publish/$topic2") {
                send(Frame.Text("message1"))
            }

            val availableTopics: Set<Topic> = mas0.get("/topics").body()
            assertEquals(setOf(topic1, topic2), availableTopics)
        }
    }
}
