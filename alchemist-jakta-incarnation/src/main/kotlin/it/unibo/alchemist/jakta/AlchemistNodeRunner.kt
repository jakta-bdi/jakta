package it.unibo.alchemist.jakta

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.jakta.actions.JaktaStepAction
import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.NodeRunner
import it.unibo.jakta.node.RuntimeNodes
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.yield
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

/**
 * A [NodeRunner] that runs every JaKtA node on a new Alchemist node of the [simulation],
 * placed at the [position] computed from its index (the order in which nodes are [run]).
 * Agents run in simulated time: each Alchemist node executes a [JaktaForAlchemistRuntime.step] at the given [rate].
 *
 * The simulation starts, on its own thread, when the first node is run,
 * and is terminated as soon as all nodes have terminated.
 * [run] returns when its node terminates or when the simulation ends, e.g. because of one of its terminators.
 * @param messaging which Alchemist nodes the messages of the agents can reach.
 * @param randomGenerator the random generator exposed to the agents by [JaktaForAlchemistRuntime.randomGenerator].
 */
class AlchemistNodeRunner<P : Position<P>, N : ExecutableNode<*>>(
    private val simulation: Simulation<Any?, P>,
    private val messaging: Messaging = Messaging.GLOBAL,
    private val rate: Double = 1.0,
    private val randomGenerator: RandomGenerator = MersenneTwister(0),
    private val position: (index: Int) -> P,
) : NodeRunner<N> {

    private val running = ConcurrentHashMap<N, CompletableDeferred<Unit>>()
    private val nextIndex = AtomicInteger()
    private val started = AtomicBoolean()

    override val nodes: Set<N> get() = running.keys.toSet()

    override suspend fun run(node: N) {
        val termination = CompletableDeferred<Unit>()
        running[node] = termination
        val index = nextIndex.getAndIncrement()
        simulation.schedule {
            val environment = simulation.environment
            val device = GenericNode(environment)
            val runtime = JaktaForAlchemistRuntime(environment, device, randomGenerator, messaging) { stop(node) }
            device.addProperty(runtime)
            runtime.setInitialJaktaNodes(RuntimeNodes(setOf(node)))
            device.addReaction(Event(device, DiracComb(rate)).also { it.actions = listOf(JaktaStepAction(runtime)) })
            environment.addNode(device, position(index))
        }
        if (started.compareAndSet(false, true)) {
            // ponytail: on single-threaded dispatchers this lets the other nodes of a MAS register before starting;
            // on multi-threaded ones, a node registering after all the others terminated finds the simulation over.
            yield()
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
        termination.await()
    }

    private fun stop(node: N) {
        running.remove(node)?.complete(Unit)
        if (running.isEmpty()) {
            simulation.terminate()
        }
    }
}
