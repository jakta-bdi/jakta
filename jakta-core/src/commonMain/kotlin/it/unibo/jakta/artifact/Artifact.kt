package it.unibo.jakta.artifact

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentID
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.node.Node
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * A CArtAgO-style artifact: an environment entity with observable properties and operations,
 * that agents use (by invoking operations) and observe (by focusing on it).
 *
 * An artifact is hosted by one node ([makeArtifact]) and can be mirrored on other nodes ([mirrorArtifact]).
 * Its operations always run on its host, one step at a time: the code between two suspension points
 * of an operation (e.g. [await] or `delay`) is atomic with respect to the other operations of the artifact.
 * The caller only suspends its own intention until the operation completes, and gets the result or the failure.
 *
 * @param name the name of the artifact, unique in the MAS: mirrors find the artifact by name.
 */
@Suppress("AbstractClassCanBeConcreteClass") // it is meant to be extended with properties and operations
abstract class Artifact(val name: String) {
    private val operations = mutableMapOf<String, Operation<*, *>>()
    private val properties = mutableMapOf<String, Any?>()
    private val observers = mutableSetOf<AgentID>()
    private val remoteObservers = mutableSetOf<AgentID>()

    // bumped whenever the state may have changed, to re-evaluate the conditions of await
    private val changes = MutableStateFlow(0L)

    private var runtime: Runtime? = null
    private val started = CompletableDeferred<Runtime>()
    private var requests = 0

    /**
     * Declares an observable property with the given [initial] value, named after the Kotlin property.
     * Every assignment is perceived by the agents focusing on the artifact as an [ArtifactEvent.PropertyChanged].
     */
    protected fun <T> observable(initial: T) =
        PropertyDelegateProvider<Artifact, ReadWriteProperty<Artifact, T>> { _, declaration ->
            properties[declaration.name] = initial
            object : ReadWriteProperty<Artifact, T> {
                @Suppress("UNCHECKED_CAST")
                override fun getValue(thisRef: Artifact, property: KProperty<*>): T = properties[property.name] as T

                override fun setValue(thisRef: Artifact, property: KProperty<*>, value: T) {
                    properties[property.name] = value
                    publish(ArtifactEvent.PropertyChanged(name, property.name, value))
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
     * Emits a [signal], perceived by the agents focusing on the artifact as an [ArtifactEvent.Signal].
     */
    protected fun signal(signal: String, value: Any? = null) = publish(ArtifactEvent.Signal(name, signal, value))

    /**
     * Suspends the current operation until the [condition] holds, letting the other operations run meanwhile.
     */
    protected suspend fun await(condition: () -> Boolean) {
        changes.first { condition() }
    }

    /**
     * Starts an internal operation: a [body] running on the host concurrently with the other operations,
     * e.g. a loop producing signals over time.
     */
    protected fun internalOperation(body: suspend () -> Unit) {
        checkNotNull(runtime) { "Internal operations can only be started by operations" }
            .host.alsoAchieve(ArtifactTask { step(body) })
    }

    internal suspend fun <A, R> execute(operation: Operation<A, R>, argument: A): R = onHost {
        @Suppress("UNCHECKED_CAST")
        if (mirror) forward(operation.name, argument) as R else step { operation.body(argument) }
    }

    internal suspend fun focus(agent: AgentID) = onHost {
        observers += agent
        if (mirror) {
            // the host replays the values to this mirror, which publishes them to all its observers
            send(homeId(name), Focus)
        } else {
            properties.forEach { (property, value) ->
                node.publishEvent(ArtifactEvent.PropertyChanged(name, property, value)) {
                    getAgentIDfromBody(it) == agent
                }
            }
        }
    }

    internal suspend fun stopFocus(agent: AgentID) = onHost { observers -= agent }

    internal fun attach(runtime: Runtime) {
        this.runtime = runtime
        started.complete(runtime)
    }

    /**
     * Maps a message received by the host into the task handling it, or null if it is not for artifacts.
     */
    internal fun taskFor(message: Message<*>): ArtifactTask? = when (val payload = message.payload) {
        is Invoke -> ArtifactTask {
            @Suppress("UNCHECKED_CAST")
            val operation = operations[payload.operation] as Operation<Any?, Any?>?
            val outcome = attempt {
                checkNotNull(operation) { "Unknown operation ${payload.operation} on $name" }
                step { operation.body(payload.argument) }
            }
            val error = outcome.exceptionOrNull()?.let { e -> e.message ?: "$e" }
            requireNotNull(runtime).send(message.sender, Outcome(payload.request, outcome.getOrNull(), error))
        }

        is Focus -> ArtifactTask {
            remoteObservers += message.sender
            properties.forEach { (property, value) ->
                requireNotNull(runtime).send(message.sender, ArtifactEvent.PropertyChanged(name, property, value))
            }
        }

        // a mirror receiving the events of the artifact it mirrors
        is ArtifactEvent -> ArtifactTask {
            if (payload is ArtifactEvent.PropertyChanged) properties[payload.property] = payload.value
            publish(payload)
        }

        else -> null
    }

    private suspend fun <R> onHost(block: suspend Runtime.() -> R): R {
        val runtime = started.await()
        val result = CompletableDeferred<R>()
        runtime.host.alsoAchieve(ArtifactTask { result.completeWith(attempt { runtime.block() }) })
        return result.await()
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

    private suspend fun <R> step(body: suspend () -> R): R = try {
        body()
    } finally {
        changes.value++
    }

    private suspend fun Runtime.forward(operation: String, argument: Any?): Any? {
        val request = requests++
        send(homeId(name), Invoke(operation, argument, request))
        val outcome = host.wait({ event ->
            ((event as? Message<*>)?.payload as? Outcome)?.takeIf { it.request == request }
        })
        checkNotNull(outcome)
        outcome.error?.let { error(it) }
        return outcome.value
    }

    private fun publish(event: ArtifactEvent) {
        changes.value++
        val runtime = runtime ?: return
        val targets = observers.toSet()
        runtime.node.publishEvent(event) { getAgentIDfromBody(it) in targets }
        remoteObservers.forEach { runtime.send(it, event) }
    }

    private fun Runtime.send(receiver: AgentID, payload: Any) =
        node.publishEvent(Message(payload, host.id)) { getAgentIDfromBody(it) == receiver }

    internal class Runtime(val node: Node<Any>, val host: MutableAgentState<Unit, ArtifactTask>, val mirror: Boolean)

    internal companion object {
        /**
         * The id of the agent hosting the artifact with the given [name], known by every node.
         */
        fun homeId(name: String): AgentID = BaseAgentID(name, "artifact:$name")
    }
}

/**
 * An operation of an [Artifact], taking an argument of type [A] and returning an [R].
 * Invoking it suspends the caller until the operation, executed by the artifact's host, completes.
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
    suspend operator fun invoke(argument: A): R = artifact.execute(this, argument)
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
     * The [artifact] has emitted the [signal], with an optional [value].
     */
    data class Signal(override val artifact: String, val signal: String, val value: Any?) : ArtifactEvent
}

/**
 * The work executed by the host of an artifact, as a goal of its (internal) agent.
 */
internal class ArtifactTask(val block: suspend (MutableAgentState<Unit, ArtifactTask>) -> Unit)

/**
 * Messages exchanged between an artifact and its mirrors.
 */
internal data class Invoke(val operation: String, val argument: Any?, val request: Int)

internal data class Outcome(val request: Int, val value: Any?, val error: String?)

internal data object Focus
