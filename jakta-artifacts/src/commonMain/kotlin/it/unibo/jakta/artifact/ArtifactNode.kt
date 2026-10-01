package it.unibo.jakta.artifact

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.dsl.node.BaseNodeBuilder
import it.unibo.jakta.dsl.node.NodeBuilders
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.BaseNode
import it.unibo.jakta.node.NodeID
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.withTimeoutOrNull

/**
 * A node that hosts artifacts and lets its agents use the artifacts hosted by other nodes: a workspace.
 *
 * Hosted artifacts run as node processes, so the node runner must run processes (e.g. `CoroutineNodeRunner`).
 * Artifacts on other nodes are used through the node network: requests and replies travel as messages,
 * that this node handles itself instead of delivering them to its agents.
 */
class ArtifactNode<Body : Any> : BaseNode<Body>() {
    private val logger = Logger.withTag("ArtifactNode")
    private val sender = BaseAgentID("artifacts", "artifacts@$id")

    // The state of the node about artifacts, only accessed by the router process, one task at a time.
    private val router = Channel<() -> Unit>(Channel.UNLIMITED)
    private val hosted = mutableMapOf<String, Artifact>()
    private val mirrors = mutableMapOf<String, Artifact>()
    private val remoteFocus = mutableMapOf<String, MutableSet<AgentID>>()
    private val pending = mutableMapOf<Long, Pending>()

    init {
        launchProcess {
            for (task in router) {
                runCatching(task).onFailure { logger.e(it) { "Failed handling artifacts on node $id" } }
            }
        }
    }

    /**
     * Hosts the [artifact] on this node, i.e. CArtAgO's `makeArtifact`.
     * @return the [artifact], which agents can also find with [ArtifactSkill.lookup].
     */
    fun <A : Artifact> makeArtifact(artifact: A): A {
        artifact.bindTo(id)
        route { hosted[artifact.name] = artifact }
        launchProcess { artifact.run() }
        return artifact
    }

    override fun handleExternalEvent(event: SystemEvent) {
        val message = (event as? SystemEvent.AgentMessage<*, *>)?.message?.payload as? ArtifactMessage
        if (message == null) super.handleExternalEvent(event) else route { handle(message) }
    }

    override fun terminateNode(error: Throwable?, nodeID: NodeID) {
        if (nodeID != id) return super.terminateNode(error, nodeID)
        // the other nodes learn that the artifacts hosted here are going away before this node stops
        route {
            hosted.keys.forEach { send(Disposed(it)) }
            shutDown(error)
        }
    }

    internal suspend fun lookup(name: String, factory: (String) -> Artifact, timeout: Duration): Artifact {
        routed { hosted[name] ?: mirrors[name] }?.let { return it }
        val home = withTimeoutOrNull(timeout) { discover(name) }
            ?: throw ArtifactException("No node hosts an artifact named $name")
        return routed { mirrors.getOrPut(name) { factory(name).also { it.bindTo(home) } } }
    }

    internal suspend fun invokeRemote(artifact: Artifact, operation: String, argument: Any?): Any? {
        val binding = bindingOf(artifact)
        return request(artifact.name) { Invoke(binding.home, artifact.name, it, id, operation, argument) }
    }

    internal suspend fun focus(artifact: Artifact, agent: AgentID) {
        val binding = bindingOf(artifact)
        if (binding.hosted) return artifact.focus(agent)
        val onReply = { snapshot: Any? ->
            remoteFocus.getOrPut(artifact.name) { mutableSetOf() } += agent
            (snapshot as Map<*, *>).forEach { (property, value) ->
                deliver(ArtifactEvent.PropertyChanged(artifact.name, property as String, value), setOf(agent))
            }
        }
        request(artifact.name, onReply) { Focus(binding.home, artifact.name, it, id, agent) }
    }

    internal suspend fun stopFocus(artifact: Artifact, agent: AgentID) {
        val binding = bindingOf(artifact)
        if (binding.hosted) return artifact.stopFocus(agent)
        val onReply = { properties: Any? ->
            remoteFocus[artifact.name]?.remove(agent)
            (properties as Set<*>).forEach {
                deliver(ArtifactEvent.PropertyRemoved(artifact.name, "$it"), setOf(agent))
            }
        }
        request(artifact.name, onReply) { StopFocus(binding.home, artifact.name, it, id, agent) }
    }

    /**
     * Delivers the [event] as a perception to the [agents] of this node.
     */
    internal fun deliver(event: ArtifactEvent, agents: Set<AgentID>) {
        if (agents.isNotEmpty()) publishEvent(event) { getAgentIDfromBody(it) in agents }
    }

    /**
     * Sends the [message] to the other nodes, without delivering it to any agent.
     */
    internal fun send(message: ArtifactMessage) = publishEvent(Message(message, sender)) { false }

    private fun Artifact.bindTo(home: NodeID) {
        check(binding == null) { "$name is already hosted or looked up" }
        binding = Binding(this@ArtifactNode, home)
    }

    private fun bindingOf(artifact: Artifact): Binding {
        val binding = checkNotNull(artifact.binding) { "${artifact.name} is neither hosted by a node nor looked up" }
        check(binding.node === this) { "${artifact.name} was looked up by another node" }
        if (binding.disposed) throw ArtifactException("${artifact.name} is no longer available")
        return binding
    }

    private fun handle(message: ArtifactMessage) {
        when (message) {
            is Lookup -> if (message.artifact in hosted) send(Reply(message.from, message.request, id, null))

            is Request -> if (message.to == id) {
                hosted[message.artifact]?.serve(message)
                    ?: send(Reply(message.from, message.request, null, "No artifact named ${message.artifact} on $id"))
            }

            is Reply -> if (message.to == id) pending.remove(message.request)?.complete(message)

            is Notify -> if (message.to == id) deliver(message.event, message.observers)

            is Disposed -> dispose(message.artifact)
        }
    }

    // the observers forget the artifact before the operations waiting for it fail
    private fun dispose(artifact: String) {
        mirrors.remove(artifact)?.let { mirror ->
            mirror.binding?.disposed = true
            remoteFocus.remove(artifact)?.let { agents ->
                mirror.properties.keys.forEach { deliver(ArtifactEvent.PropertyRemoved(artifact, it), agents) }
            }
        }
        pending.filterValues { it.artifact == artifact }.forEach { (request, waiting) ->
            pending.remove(request)
            waiting.reply.completeExceptionally(ArtifactException("$artifact is no longer available"))
        }
    }

    // looks the artifact up on the other nodes until one replies, the first replies may get lost while nodes start
    private suspend fun discover(name: String): NodeID {
        while (true) {
            val home = withTimeoutOrNull(LOOKUP_RETRY) { request(name) { Lookup(name, it, id) } }
            if (home is NodeID) return home
        }
    }

    // sends the message built with a new request id, and waits for the reply, after handling it with onReply
    private suspend fun request(
        artifact: String,
        onReply: (Any?) -> Unit = {},
        message: (Long) -> ArtifactMessage,
    ): Any? {
        val request = Random.nextLong()
        val reply = CompletableDeferred<Any?>()
        route {
            pending[request] = Pending(artifact, reply, onReply)
            send(message(request))
        }
        return try {
            reply.await()
        } finally {
            route { pending.remove(request) }
        }
    }

    private fun route(task: () -> Unit) {
        router.trySend(task)
    }

    private suspend fun <R> routed(task: () -> R): R {
        val result = CompletableDeferred<R>()
        route { result.completeWith(runCatching(task)) }
        return result.await()
    }

    private fun shutDown(error: Throwable?) = super.terminateNode(error, id)

    private class Pending(val artifact: String, val reply: CompletableDeferred<Any?>, val onReply: (Any?) -> Unit) {
        fun complete(message: Reply) {
            if (message.error != null) {
                reply.completeExceptionally(ArtifactException(message.error))
            } else {
                onReply(message.value)
                reply.complete(message.value)
            }
        }
    }

    private companion object {
        val LOOKUP_RETRY = 1.seconds
    }
}

/**
 * A node builder for [ArtifactNode]s.
 */
class ArtifactNodeBuilder<Body : Any>(override val node: ArtifactNode<Body> = ArtifactNode()) :
    BaseNodeBuilder<Body, ArtifactNode<Body>>(node)

/**
 * Creates builders of [ArtifactNode]s, to use with `mas(...)` and `node(...)`.
 */
fun <Body : Any> NodeBuilders.artifactNode(): () -> ArtifactNodeBuilder<Body> = { ArtifactNodeBuilder() }

/**
 * The messages that nodes exchange about artifacts.
 */
internal sealed interface ArtifactMessage

/**
 * Asks the nodes which one hosts the [artifact].
 */
internal data class Lookup(val artifact: String, val request: Long, val from: NodeID) : ArtifactMessage

/**
 * A [request] from the node [from] to the node [to], hosting the [artifact].
 */
internal sealed interface Request : ArtifactMessage {
    val to: NodeID
    val artifact: String
    val request: Long
    val from: NodeID
}

internal data class Invoke(
    override val to: NodeID,
    override val artifact: String,
    override val request: Long,
    override val from: NodeID,
    val operation: String,
    val argument: Any?,
) : Request

internal data class Focus(
    override val to: NodeID,
    override val artifact: String,
    override val request: Long,
    override val from: NodeID,
    val agent: AgentID,
) : Request

internal data class StopFocus(
    override val to: NodeID,
    override val artifact: String,
    override val request: Long,
    override val from: NodeID,
    val agent: AgentID,
) : Request

/**
 * The reply to the [request] of the node [to]: a [value], or an [error] message.
 */
internal data class Reply(val to: NodeID, val request: Long, val value: Any?, val error: String?) : ArtifactMessage

/**
 * An [event] of an artifact, for the [observers] on the node [to].
 */
internal data class Notify(val to: NodeID, val event: ArtifactEvent, val observers: Set<AgentID>) : ArtifactMessage

/**
 * The [artifact] is no longer available, because the node hosting it is terminating.
 */
internal data class Disposed(val artifact: String) : ArtifactMessage
