package it.unibo.jakta.mqtt

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentMessageEvent
import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue
import it.unibo.jakta.node.MessageFilter
import it.unibo.jakta.node.NodeNetwork
import it.unibo.jakta.node.NodeSubscription
import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

/**
 * A [NodeNetwork] connecting the nodes of a MAS running in different processes through an MQTT broker.
 *
 * Agent messages are serialized as JSON and published on the `<topicPrefix>/messages` topic, which every node
 * subscribes to: each node then delivers them to its agents that satisfy the message filter.
 * Payloads and filters must be registered as polymorphic subclasses of [Any] and [MessageFilter] in [serializers]
 * ([String] payloads and the built-in filters are always registered).
 * Messages that cannot be serialized, as well as all the other system events, are only delivered locally.
 *
 * Nodes announce themselves on the retained `<topicPrefix>/nodes/<nodeName>` topic:
 * [subscribe] suspends until all the [peers] are online, so that no message is sent to a node not yet listening.
 *
 * @param broker the URI of the MQTT broker, e.g. `tcp://localhost:1883`.
 * @param nodeName the id of the node using this network.
 * @param peers the ids of all the nodes of the MAS, including [nodeName].
 * @param serializers the serializers of the message payloads and filters.
 * @param topicPrefix the prefix of the MQTT topics, to share a broker among MASs.
 */
class MqttNetwork(
    broker: String,
    private val nodeName: String,
    private val peers: Set<String>,
    serializers: SerializersModule = EmptySerializersModule(),
    topicPrefix: String = "jakta",
) : NodeNetwork,
    AutoCloseable {

    private val logger = Logger.withTag("MqttNetwork[$nodeName]")

    private val messagesTopic = "$topicPrefix/messages"
    private val nodesTopic = "$topicPrefix/nodes"
    private val presenceTopic = "$nodesTopic/$nodeName"

    private val json = Json {
        useArrayPolymorphism = true
        serializersModule = SerializersModule {
            include(builtinSerializers)
            include(serializers)
        }
    }

    private val lock = Any()
    private val subscribers = mutableListOf<EventQueue<SystemEvent>>()
    private val pending = mutableListOf<SystemEvent>()
    private val onlinePeers = mutableSetOf<String>()
    private val allPeersOnline = CompletableDeferred<Unit>()

    private val client = MqttAsyncClient(broker, "$topicPrefix-$nodeName", MemoryPersistence())

    init {
        client.setCallback(
            object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String) {
                    // subscriptions do not survive reconnections of clean sessions
                    client.subscribe(arrayOf(messagesTopic, "$nodesTopic/+"), intArrayOf(QOS, QOS))
                    client.publish(presenceTopic, nodeName.encodeToByteArray(), QOS, true)
                }

                override fun connectionLost(cause: Throwable?) {
                    logger.w { "Connection to the broker lost, reconnecting: $cause" }
                }

                override fun messageArrived(topic: String, message: MqttMessage) {
                    // throwing here would make Paho drop the connection
                    runCatching { onMessage(topic, message.payload) }
                        .onFailure { logger.e(it) { "Cannot handle a message on topic $topic" } }
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) = Unit
            },
        )
        client.setBufferOpts(DisconnectedBufferOptions().apply { isBufferEnabled = true })
        connect(
            MqttConnectOptions().apply {
                isCleanSession = true
                isAutomaticReconnect = true
                maxInflight = MAX_INFLIGHT
                setWill(presenceTopic, ByteArray(0), QOS, true)
            },
        )
        if (peers.isEmpty()) allPeersOnline.complete(Unit)
    }

    private fun connect(options: MqttConnectOptions) {
        repeat(CONNECTION_ATTEMPTS) {
            try {
                client.connect(options).waitForCompletion()
                return
            } catch (e: MqttException) {
                logger.i { "Broker ${client.serverURI} not reachable (${e.message}), retrying" }
                Thread.sleep(CONNECTION_RETRY_MILLIS)
            }
        }
        error("Cannot connect to the MQTT broker at ${client.serverURI}")
    }

    private fun onMessage(topic: String, payload: ByteArray) {
        if (topic == messagesTopic) {
            val envelope = json.decodeFromString(Envelope.serializer(), payload.decodeToString())
            @Suppress("UNCHECKED_CAST")
            deliverLocally(
                AgentMessageEvent(
                    AgentEvent.External.Message(envelope.payload, envelope.sender),
                    envelope.filter as MessageFilter<Any>,
                ),
            )
        } else {
            synchronized(lock) {
                val peer = topic.removePrefix("$nodesTopic/")
                if (payload.isEmpty()) onlinePeers -= peer else onlinePeers += peer
                if (onlinePeers.containsAll(peers)) allPeersOnline.complete(Unit)
            }
        }
    }

    private fun deliverLocally(event: SystemEvent) = synchronized(lock) {
        if (subscribers.isEmpty()) pending += event else subscribers.forEach { it.send(event) }
    }

    private fun publish(event: SystemEvent.AgentMessage<*, *>) {
        val envelope = Envelope(event.message.sender, event.filter, event.message.payload)
        val encoded = try {
            json.encodeToString(Envelope.serializer(), envelope)
        } catch (e: SerializationException) {
            logger.w { "Delivering ${event.message} only to the local node, it cannot be serialized: ${e.message}" }
            deliverLocally(event)
            return
        }
        client.publish(messagesTopic, encoded.encodeToByteArray(), QOS, false)
    }

    override suspend fun subscribe(): NodeSubscription {
        if (!allPeersOnline.isCompleted) logger.i { "Waiting for nodes ${peers - onlinePeers}" }
        allPeersOnline.await()
        return addSubscriber()
    }

    override fun trySubscribe(): NodeSubscription? = if (allPeersOnline.isCompleted) addSubscriber() else null

    private fun addSubscriber(): NodeSubscription {
        val queue = UnlimitedChannelQueue<SystemEvent>()
        synchronized(lock) {
            subscribers += queue
            pending.forEach { queue.send(it) }
            pending.clear()
        }
        return object : NodeSubscription {
            override val queue: EventQueue<SystemEvent> = queue

            override suspend fun close() {
                synchronized(lock) { subscribers -= queue }
            }
        }
    }

    override suspend fun send(event: SystemEvent) {
        trySend(event)
    }

    override fun trySend(event: SystemEvent): Boolean {
        when (event) {
            is SystemEvent.AgentMessage<*, *> -> publish(event)

            is SystemEvent.AgentAddition<*, *> -> {
                if (event.nodeID.id != nodeName) {
                    logger.e { "Agents cannot be added to remote nodes, ${event.executableAgent.id} is lost" }
                }
                deliverLocally(event)
            }

            else -> deliverLocally(event)
        }
        return true
    }

    /**
     * Clears the presence of the node and disconnects from the broker.
     */
    override fun close() {
        // a clean disconnection does not trigger the last will
        client.publish(presenceTopic, ByteArray(0), QOS, true).waitForCompletion()
        client.disconnect(QUIESCE_MILLIS).waitForCompletion()
        client.close()
    }

    private companion object {
        const val QOS = 1
        const val MAX_INFLIGHT = 1000
        const val CONNECTION_ATTEMPTS = 30
        const val CONNECTION_RETRY_MILLIS = 1000L
        const val QUIESCE_MILLIS = 5000L
    }
}
