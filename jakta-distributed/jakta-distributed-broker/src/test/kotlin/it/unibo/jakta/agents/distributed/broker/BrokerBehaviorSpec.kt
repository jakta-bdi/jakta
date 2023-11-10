package it.unibo.jakta.agents.distributed.broker

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import it.unibo.jakta.agents.distributed.broker.model.MasID
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

            val expectedMasID0 = MasID("Mas0")
            val expectedMasID1 = MasID("Mas1")

            val mas0ID: MasID = mas0.get("/uniqueID").body()
            assertEquals(expectedMasID0, mas0ID)

            val mas1ID: MasID = mas1.get("/uniqueID").body()
            assertEquals(expectedMasID1, mas1ID)

            mas0.webSocket("/publish/${mas0ID.id}") {
                send(Frame.Text("message0"))
            }

            mas1.webSocket("/publish/${mas1ID.id}") {
                send(Frame.Text("message1"))
            }

            val availableTopics: Set<Topic> = mas0.get("/topics").body()
            assertEquals(emptySet(), availableTopics)
        }
    }
}
