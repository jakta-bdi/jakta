package it.unibo.jakta.mqtt

import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentMessageEvent
import it.unibo.jakta.event.AgentRemovalEvent
import it.unibo.jakta.kqml.Tell
import it.unibo.jakta.kqml.kqmlSerializersModule
import it.unibo.jakta.node.BroadcastFrom
import it.unibo.jakta.node.MessageFilter
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeSubscription
import it.unibo.jakta.node.SendTo
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Integer
import it.unibo.tuprolog.core.Struct
import java.net.ServerSocket
import java.util.Properties
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic

class TestMqttNetwork {

    /**
     * A custom filter selecting the agents whose (integer) position is within [radius] from [center].
     */
    @Serializable
    data class Within(val center: Int, val radius: Int) : MessageFilter<Any> {
        override fun accept(node: Node<out Any>, agent: AgentID, body: Any) = abs(body as Int - center) <= radius
    }

    private val alice = BaseAgentID("alice", "alice")
    private val bob = BaseAgentID("bob", "bob")

    private val serializers = kqmlSerializersModule +
        SerializersModule { polymorphic(MessageFilter::class) { subclass(Within::class, Within.serializer()) } }

    private val port = ServerSocket(0).use { it.localPort }
    private val broker = Server()

    @BeforeTest
    fun startBroker() {
        val config = Properties().apply {
            setProperty("host", "127.0.0.1")
            setProperty("port", "$port")
            setProperty("persistence_enabled", "false")
        }
        broker.startServer(MemoryConfig(config))
    }

    @AfterTest
    fun stopBroker() = broker.stopServer()

    // Runs the test with two nodes, ping and pong, each with its own connection to the broker.
    private fun withNodes(test: suspend (ping: MqttNetwork, NodeSubscription, NodeSubscription) -> Unit) {
        val peers = setOf("ping", "pong")
        MqttNetwork("tcp://127.0.0.1:$port", "ping", peers, serializers).use { ping ->
            MqttNetwork("tcp://127.0.0.1:$port", "pong", peers, serializers).use { pong ->
                runBlocking {
                    withTimeout(10.seconds) { test(ping, ping.subscribe(), pong.subscribe()) }
                }
            }
        }
    }

    @Test
    fun `messages reach every node with their payload and filter`() = withNodes { ping, pingNode, pongNode ->
        val tell =
            AgentMessageEvent(Message(Tell(setOf(Fact.of(Struct.of("ball", Integer.of(1))))), alice), SendTo(bob))
        val near = AgentMessageEvent(Message("hi", alice), Within(0, 10))
        ping.send(tell)
        ping.send(near)
        // the sender node receives its own messages back from the broker, too
        listOf(pingNode, pongNode).forEach {
            assertEquals(listOf(tell, near), listOf(it.queue.next(), it.queue.next()))
        }
    }

    @Test
    fun `lambda filters and system events stay on the local node`() = withNodes { ping, pingNode, pongNode ->
        val local = AgentMessageEvent(Message("secret", alice), MessageFilter<Any> { _, _, _ -> true })
        val removal = AgentRemovalEvent(alice)
        val broadcast = AgentMessageEvent(Message("public", alice), BroadcastFrom(alice))
        ping.send(local)
        ping.send(removal)
        ping.send(broadcast)
        assertEquals(listOf(local, removal, broadcast), List(3) { pingNode.queue.next() })
        assertEquals(broadcast, pongNode.queue.next())
    }
}
