package it.unibo.jakta.agents.distributed.broker

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import it.unibo.jakta.agents.distributed.broker.model.Topic
import it.unibo.jakta.agents.distributed.common.Error
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class BrokerBehaviorSpec {
    @Test
    fun testTopicPublication() {
        testApplication {
            val fooMas = createClient {
                install(ContentNegotiation) {
                    json()
                }
                install(WebSockets)
            }
            val barMas = createClient {
                install(ContentNegotiation) {
                    json()
                }
                install(WebSockets)
            }
            val fooTopic = "fooTopic"
            val barTopic = "barTopic"
            val message = "testMessage"
            val fooSubscribeSession = fooMas.webSocketSession("/subscribe/$barTopic")
            val barSubscribeSession = barMas.webSocketSession("/subscribe/$fooTopic")
            val fooPublishSession = fooMas.webSocketSession("/publish/$fooTopic")
            val barPublishSession = barMas.webSocketSession("/publish/$barTopic")
            val availableTopics: Set<Topic> = fooMas.get("/topics").body()

            assertEquals(setOf(fooTopic, barTopic), availableTopics)

            fooSubscribeSession.send(Frame.Text(message))
            val response = fooSubscribeSession.incoming.receive()
            assertEquals(Error.BAD_REQUEST, Json.decodeFromString((response as Frame.Text).readText()))

            fooPublishSession.send(Frame.Text(message))
            val response2 = barSubscribeSession.incoming.receive()
            assertEquals(message, (response2 as Frame.Text).readText())

            barPublishSession.send(Frame.Text(message))
            val response3 = fooSubscribeSession.incoming.receive()
            assertEquals(message, (response3 as Frame.Text).readText())
        }
    }
}
