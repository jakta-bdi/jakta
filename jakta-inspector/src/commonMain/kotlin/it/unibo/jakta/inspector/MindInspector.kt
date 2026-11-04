package it.unibo.jakta.inspector

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycleObserver
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.CoroutineNodeRunner
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.node.NodeNetwork
import it.unibo.jakta.node.SharedMemoryNetwork
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val DEFAULT_HISTORY_SIZE = 500
private const val DEFAULT_MESSAGE_LOG_SIZE = 1000

/**
 * Records what the agents of a multi-agent system have in mind, event after event, and the messages they exchange.
 *
 * It observes the agents' lifecycles (pass it to a [CoroutineNodeRunner], or use [runner]) and the network the nodes
 * share ([tap]). Each agent records a snapshot of its mind after every event it handles, in its own loop: the
 * snapshot copies the collections, not the beliefs, which should be immutable to be shown as they were.
 *
 * @param historySize how many snapshots each agent keeps, dropping the oldest ones.
 * @param messageLogSize how many messages are kept, dropping the oldest ones.
 * @param messageKind the kind of a message from its payload, which colours it (by default its class name).
 */
class MindInspector(
    private val historySize: Int = DEFAULT_HISTORY_SIZE,
    private val messageLogSize: Int = DEFAULT_MESSAGE_LOG_SIZE,
    private val messageKind: (Any) -> String = { it::class.simpleName?.lowercase() ?: "message" },
) : AgentLifecycleObserver {
    private val start = TimeSource.Monotonic.markNow()
    private val mutableAgents = MutableStateFlow<List<InspectedAgent>>(emptyList())
    private val mutableMessages = MutableStateFlow<List<InspectedMessage>>(emptyList())

    /**
     * The agents seen so far, in order of appearance.
     */
    val agents: StateFlow<List<InspectedAgent>> = mutableAgents.asStateFlow()

    /**
     * The most recent messages, oldest first.
     */
    val messages: StateFlow<List<InspectedMessage>> = mutableMessages.asStateFlow()

    /**
     * Wraps [network] so that the inspector sees the agents created and the messages sent on it.
     */
    fun tap(network: NodeNetwork): NodeNetwork = object : NodeNetwork by network {
        override suspend fun send(event: SystemEvent) {
            // recorded before sending, so that a message is logged before anyone handles it
            observe(event)
            network.send(event)
        }

        // ponytail: recorded after a successful send, so a quick receiver may log the message first, as received only
        override fun trySend(event: SystemEvent): Boolean = network.trySend(event).also { sent ->
            if (sent) observe(event)
        }
    }

    /**
     * A runner whose agents are inspected, on the [network] tapped by the inspector.
     */
    fun <Body : Any, N : ExecutableNode<Body>> runner(
        network: NodeNetwork = SharedMemoryNetwork(),
    ): CoroutineNodeRunner<Body, N> = CoroutineNodeRunner(tap(network), this)

    override fun onEventHandled(agent: ExecutableAgent<*, *>, event: AgentEvent) {
        val time = start.elapsedNow()
        agentFor(agent.id, node = null).record(agent, event, time)
        if (event is Message<*>) received(event, agent.id, time)
    }

    private fun observe(event: SystemEvent) {
        val time = start.elapsedNow()
        when (event) {
            is SystemEvent.AgentAddition<*, *> ->
                agentFor(event.executableAgent.id, event.nodeID).recordCreation(event.executableAgent, time)

            is SystemEvent.AgentMessage<*, *> -> mutableMessages.update { log ->
                (log + InspectedMessage(time, event.message, messageKind(event.message.payload))).takeLast(
                    messageLogSize,
                )
            }

            else -> Unit
        }
    }

    // the same message object reaches every receiver of a send; an equal one if the network copies messages
    private fun received(message: Message<*>, receiver: AgentID, time: Duration) = mutableMessages.update { log ->
        val index = log.indexOfFirst { it.message === message && receiver !in it.receivers }
            .takeIf { it >= 0 }
            ?: log.indexOfFirst { it.message == message && receiver !in it.receivers }
        if (index < 0) {
            // not sent through the tapped network: logged when handled
            val handled = InspectedMessage(time, message, messageKind(message.payload), listOf(receiver))
            (log + handled).takeLast(messageLogSize)
        } else {
            log.toMutableList().apply { this[index] = this[index].copy(receivers = this[index].receivers + receiver) }
        }
    }

    // ponytail: linear scan on every event, a map if MAS grow to thousands of agents
    private fun agentFor(id: AgentID, node: NodeID?): InspectedAgent {
        mutableAgents.value.find { it.id == id }?.let { return it }
        val created = InspectedAgent(id, node, historySize)
        mutableAgents.update { agents -> if (agents.any { it.id == id }) agents else agents + created }
        return mutableAgents.value.first { it.id == id }
    }
}

/**
 * A message sent by an agent.
 *
 * @property time when it was sent, since the inspector started.
 * @property message the message, with its sender and payload.
 * @property kind the kind of message, such as `tell`, which colours it.
 * @property receivers the agents that handled it so far: none if it was lost or is still on its way.
 */
data class InspectedMessage(
    val time: Duration,
    val message: Message<*>,
    val kind: String,
    val receivers: List<AgentID> = emptyList(),
)
