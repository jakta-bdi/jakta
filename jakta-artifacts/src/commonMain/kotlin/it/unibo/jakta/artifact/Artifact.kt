package it.unibo.jakta.artifact

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.node.NodeID
import kotlin.concurrent.Volatile
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * A CArtAgO-style artifact: an environment entity with observable properties and operations,
 * that agents use (by invoking its operations) and observe (by focusing on it) through the [ArtifactSkill].
 *
 * An artifact is hosted by one [ArtifactNode] ([ArtifactNode.makeArtifact]), which runs it as a node process,
 * and it can be used from other nodes too ([ArtifactSkill.lookup]).
 * Its operations run one *step* at a time: the code between two suspension points of an operation
 * (e.g. [await] or `delay`) is atomic with respect to the other operations of the artifact.
 * The changes to the observable properties are published at the end of each step, or by a [signal],
 * and they are rolled back if the operation fails.
 *
 * @param name the name of the artifact, unique in the MAS: other nodes look it up by name.
 */
@Suppress("AbstractClassCanBeConcreteClass") // it is meant to be extended with properties and operations
abstract class Artifact(val name: String) {
    private val operations = mutableMapOf<String, Operation<*, *>>()
    internal val properties = mutableMapOf<String, Any?>()

    // the values of the properties changed in the current step, before the change
    private val previous = mutableMapOf<String, Any?>()
    private val localObservers = mutableSetOf<AgentID>()
    private val remoteObservers = mutableMapOf<NodeID, MutableSet<AgentID>>()

    // bumped at the end of every step that may have changed the state, to re-evaluate the conditions of await
    private val version = MutableStateFlow(0L)
    private var quiet = false

    private val inbox = Channel<suspend () -> Unit>(Channel.UNLIMITED)
    private var scope: CoroutineScope? = null

    @Volatile
    internal var binding: Binding? = null

    /**
     * Declares an observable property with the given [initial] value, named after the Kotlin property.
     * The agents focusing on the artifact perceive its changes as [ArtifactEvent.PropertyChanged].
     */
    protected fun <T> observable(initial: T) =
        PropertyDelegateProvider<Artifact, ReadWriteProperty<Artifact, T>> { _, declaration ->
            properties[declaration.name] = initial
            object : ReadWriteProperty<Artifact, T> {
                @Suppress("UNCHECKED_CAST")
                override fun getValue(thisRef: Artifact, property: KProperty<*>): T = properties[property.name] as T

                override fun setValue(thisRef: Artifact, property: KProperty<*>, value: T) {
                    if (property.name !in previous) previous[property.name] = properties[property.name]
                    properties[property.name] = value
                }
            }
        }

    /**
     * Declares an operation without arguments, named after the Kotlin property.
     */
    protected fun <R> operation(
        body: suspend () -> R,
    ): PropertyDelegateProvider<Artifact, ReadOnlyProperty<Artifact, Operation<Unit, R>>> =
        operationWith<Unit, R> { body() }

    /**
     * Declares an operation taking an argument of type [A], named after the Kotlin property.
     */
    protected fun <A, R> operationWith(body: suspend (A) -> R) =
        PropertyDelegateProvider<Artifact, ReadOnlyProperty<Artifact, Operation<A, R>>> { _, declaration ->
            val operation = Operation(this, declaration.name, body)
            operations[declaration.name] = operation
            ReadOnlyProperty { _, _ -> operation }
        }

    /**
     * Publishes the pending changes and then emits a [signal],
     * perceived by the agents focusing on the artifact as an [ArtifactEvent.Signal].
     */
    protected fun signal(signal: String, value: Any? = null) {
        commit()
        publish(ArtifactEvent.Signal(name, signal, value))
    }

    /**
     * Suspends the current operation until the [condition] holds, letting the other operations run meanwhile.
     */
    protected suspend fun await(condition: () -> Boolean) {
        var recheck = false
        while (!condition()) {
            quiet = recheck // a step that only checked the condition again changes nothing
            val seen = version.value
            version.first { it != seen }
            recheck = true
        }
    }

    /**
     * Starts an internal operation: a [body] running concurrently with the other operations of the artifact,
     * one step at a time like them, e.g. a loop updating a property over time.
     */
    protected fun internalOperation(body: suspend () -> Unit) {
        checkNotNull(scope) { "Internal operations can only be started by operations" }
            .launch { runOperation(body).getOrThrow() }
    }

    /**
     * Runs the artifact until the node hosting it terminates.
     */
    internal suspend fun run(): Unit = coroutineScope {
        val dispatcher = checkNotNull(coroutineContext[ContinuationInterceptor] as? CoroutineDispatcher) {
            "Artifacts need a CoroutineDispatcher to run"
        }
        val artifactScope = CoroutineScope(
            coroutineContext + SupervisorJob(coroutineContext.job) + StepDispatcher(dispatcher, ::endStep),
        )
        scope = artifactScope
        for (task in inbox) artifactScope.launch { task() }
    }

    internal suspend fun <A, R> invoke(operation: Operation<A, R>, argument: A): R {
        val binding = checkNotNull(binding) { "$name is neither hosted by a node nor looked up" }
        @Suppress("UNCHECKED_CAST")
        return when {
            binding.hosted -> onSteps { runOperation { operation.body(argument) }.getOrThrow() }
            else -> binding.node.invokeRemote(this, operation.name, argument) as R
        }
    }

    internal suspend fun focus(agent: AgentID) = onSteps {
        localObservers += agent
        properties.forEach { (property, value) ->
            requireNotNull(binding).node.deliver(ArtifactEvent.PropertyChanged(name, property, value), setOf(agent))
        }
    }

    internal suspend fun stopFocus(agent: AgentID) = onSteps {
        localObservers -= agent
        properties.keys.forEach {
            requireNotNull(binding).node.deliver(ArtifactEvent.PropertyRemoved(name, it), setOf(agent))
        }
    }

    /**
     * Serves a [request] coming from another node, replying to it.
     */
    internal fun serve(request: Request) {
        inbox.trySend {
            val outcome: Result<Any?> = when (request) {
                is Invoke -> runOperation {
                    @Suppress("UNCHECKED_CAST")
                    val operation = operations[request.operation] as Operation<Any?, Any?>?
                    checkNotNull(operation) { "Unknown operation ${request.operation} on $name" }.body(request.argument)
                }

                is Focus -> {
                    remoteObservers.getOrPut(request.from) { mutableSetOf() } += request.agent
                    Result.success(properties.toMap())
                }

                is StopFocus -> {
                    remoteObservers[request.from]?.remove(request.agent)
                    Result.success(properties.keys.toSet())
                }
            }
            val error = outcome.exceptionOrNull()?.let { it.message ?: "$it" }
            requireNotNull(binding).node.send(Reply(request.from, request.request, outcome.getOrNull(), error))
        }
    }

    private suspend fun <R> onSteps(block: suspend () -> R): R {
        val result = CompletableDeferred<R>()
        inbox.trySend { result.completeWith(attempt(block)) }
        return result.await()
    }

    private suspend fun <R> runOperation(body: suspend () -> R): Result<R> =
        attempt(body).also { if (it.isSuccess) commit() else rollback() }

    private fun endStep() {
        commit()
        if (!quiet) version.value++
        quiet = false
    }

    private fun commit() {
        val events = previous.keys.map { ArtifactEvent.PropertyChanged(name, it, properties[it]) }
        previous.clear()
        events.forEach(::publish)
    }

    private fun rollback() {
        properties += previous
        previous.clear()
    }

    private fun publish(event: ArtifactEvent) {
        val node = binding?.node ?: return
        node.deliver(event, localObservers.toSet())
        remoteObservers.forEach { (to, agents) ->
            if (agents.isNotEmpty()) node.send(Notify(to, event, agents.toSet()))
        }
    }
}

/**
 * Where an artifact lives: the [node] using it, and its [home] node, which hosts it.
 */
internal class Binding(val node: ArtifactNode<*>, val home: NodeID) {
    val hosted: Boolean get() = home == node.id

    @Volatile
    var disposed: Boolean = false
}

/**
 * An operation of an [Artifact], taking an argument of type [A] and returning an [R].
 * Invoking it suspends the caller until the artifact executes it, wherever the artifact is hosted.
 * @property name the name of the operation.
 */
class Operation<A, R> internal constructor(
    private val artifact: Artifact,
    val name: String,
    internal val body: suspend (A) -> R,
) {
    /**
     * Executes the operation with the given [argument], rethrowing its failure.
     */
    suspend operator fun invoke(argument: A): R = artifact.invoke(this, argument)
}

/**
 * Executes an operation without arguments.
 */
suspend operator fun <R> Operation<Unit, R>.invoke(): R = invoke(Unit)

/**
 * The perceptions generated by an [Artifact] for the agents focusing on it.
 */
sealed interface ArtifactEvent : Perception {
    /**
     * The name of the artifact generating the event.
     */
    val artifact: String

    /**
     * The observable [property] of the [artifact] has now the given [value].
     */
    data class PropertyChanged(override val artifact: String, val property: String, val value: Any?) : ArtifactEvent

    /**
     * The observable [property] of the [artifact] is no longer perceived,
     * because the agent stopped focusing on the artifact or the artifact is no longer available.
     */
    data class PropertyRemoved(override val artifact: String, val property: String) : ArtifactEvent

    /**
     * The [artifact] has emitted the [signal], with an optional [value].
     */
    data class Signal(override val artifact: String, val signal: String, val value: Any?) : ArtifactEvent
}

/**
 * The failure of an artifact used from another node: an operation that failed there, or an unavailable artifact.
 */
class ArtifactException(message: String) : Exception(message)

/**
 * Runs the steps of an artifact one at a time on the node [dispatcher], calling [endStep] after each of them.
 * A step runs a coroutine of the artifact until it suspends.
 */
@OptIn(InternalCoroutinesApi::class) // delays must follow the node's time, e.g. virtual time in tests
private class StepDispatcher(dispatcher: CoroutineDispatcher, private val endStep: () -> Unit) :
    CoroutineDispatcher(),
    Delay by (dispatcher as? Delay ?: error("The dispatcher running the node must implement Delay")) {

    private val serial = dispatcher.limitedParallelism(1)

    override fun dispatch(context: CoroutineContext, block: Runnable) = serial.dispatch(
        context,
        Runnable {
            try {
                block.run()
            } finally {
                endStep()
            }
        },
    )
}

// runCatching, letting cancellation propagate
@Suppress("TooGenericExceptionCaught")
private suspend fun <R> attempt(block: suspend () -> R): Result<R> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
