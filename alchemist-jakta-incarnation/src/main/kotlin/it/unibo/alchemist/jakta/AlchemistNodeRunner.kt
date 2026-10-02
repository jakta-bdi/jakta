package it.unibo.alchemist.jakta

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.jakta.actions.JaktaStepAction
import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeID
import it.unibo.jakta.node.NodeRunner
import it.unibo.jakta.node.RuntimeNodes
import it.unibo.jakta.situated.Situated
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitAll
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

/**
 * A [NodeRunner] that runs every JaKtA node on its own, new, Alchemist node of the [simulation].
 * Each Alchemist node executes a [JaktaForAlchemistRuntime.step] according to the [timeDistribution] of its JaKtA node,
 * so agents run in simulated time.
 * Each node is placed at its [position] or, by default,
 * at the [Situated.position] of the first agent of the node with a [Situated] body.
 * Agents reach Alchemist through the skills given by [skillsFor].
 *
 * The simulation starts, on its own thread, when the nodes of a MAS are run with [runAll],
 * and is terminated as soon as all nodes have terminated.
 * Nodes can also join a running simulation with [run]: if the simulation terminates before, they fail to start.
 * [runAll] returns when all its nodes terminate or when the simulation ends, e.g. because of one of its terminators.
 * @param messaging which Alchemist nodes the messages of the agents can reach.
 * @param randomGenerator the random generator of the agents, given by the random skill of [skillsFor]
 * and by [JaktaForAlchemistRuntime.randomGenerator].
 */
class AlchemistNodeRunner<P : Position<P>, N : ExecutableNode<*>>(
    private val simulation: Simulation<Any?, P>,
    private val messaging: Messaging = Messaging.GLOBAL,
    private val randomGenerator: RandomGenerator = MersenneTwister(0),
    private val timeDistribution: (N) -> TimeDistribution<Any?> = { DiracComb(1.0) },
    private val position: ((N) -> P)? = null,
) : NodeRunner<N> {

    private val running = ConcurrentHashMap<N, CompletableDeferred<Unit>>()
    private val runtimes = ConcurrentHashMap<NodeID, JaktaForAlchemistRuntime<P>>()
    private val started = AtomicBoolean()

    override val nodes: Set<N> get() = running.keys.toSet()

    /**
     * The situated skills for the agents of [node], backed by the Alchemist node running it.
     * They can be installed before the node runs, but only used while it runs.
     */
    fun skillsFor(node: Node<*>): AlchemistSkills<P> = AlchemistSkills {
        checkNotNull(runtimes[node.id]) { "The JaKtA node ${node.id} is not running in this simulation" }
    }

    override suspend fun run(node: N) = runAll(listOf(node))

    override suspend fun runAll(nodes: Collection<N>) {
        val terminations = nodes.map { register(it) }
        if (started.compareAndSet(false, true)) {
            start()
        }
        terminations.awaitAll()
    }

    private fun register(node: N): CompletableDeferred<Unit> {
        val termination = CompletableDeferred<Unit>()
        running[node] = termination
        simulation.schedule {
            val environment = simulation.environment
            val device = GenericNode(environment)
            val runtime = JaktaForAlchemistRuntime(environment, device, randomGenerator, messaging) { stop(node) }
            device.addProperty(runtime)
            runtimes[node.id] = runtime
            runtime.setInitialJaktaNodes(RuntimeNodes(setOf(node)))
            val reaction = Event(device, timeDistribution(node))
            reaction.actions = listOf(JaktaStepAction(runtime))
            device.addReaction(reaction)
            environment.addNode(device, position?.invoke(node) ?: environment.embodiedPosition(node))
        }
        return termination
    }

    private fun Environment<Any?, P>.embodiedPosition(node: N): P {
        val start = node.agents.values.firstNotNullOfOrNull { (it as? Situated)?.position }
        checkNotNull(start) {
            "Cannot place the JaKtA node ${node.id}: " +
                "embody one of its agents as Situated, or give the runner a position function"
        }
        return makePosition(start.values)
    }

    private fun start() {
        simulation.addOutputMonitor(
            object : OutputMonitor<Any?, P> {
                override fun finished(environment: Environment<Any?, P>, time: Time, step: Long) {
                    running.values.forEach { pending ->
                        simulation.error.ifPresentOrElse(pending::completeExceptionally) { pending.complete(Unit) }
                    }
                    running.clear()
                }
            },
        )
        simulation.play()
        thread(isDaemon = true, name = "JaKtA Alchemist simulation") { simulation.run() }
    }

    private fun stop(node: N) {
        running.remove(node)?.complete(Unit)
        if (running.isEmpty()) {
            simulation.terminate()
        }
    }
}
