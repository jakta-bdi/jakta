package it.unibo.alchemist.jakta.properties

import co.touchlab.kermit.Logger
import it.unibo.alchemist.jakta.AlchemistSkills
import it.unibo.alchemist.jakta.Messaging
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.alchemist.AlchemistDispatcher
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.RuntimeNodes
import it.unibo.jakta.situated.Coordinates
import it.unibo.jakta.situated.Situated
import kotlin.random.Random
import kotlin.random.asKotlinRandom
import org.apache.commons.math3.random.RandomAdaptor
import org.apache.commons.math3.random.RandomGenerator

/** One Alchemist Node may contain more than one Jakta Node.
 * This Alchemist property connects JaKtA metamodel to alchemist representation:
 * it steps the agents of the hosted JaKtA nodes and exchanges their system events with the other Alchemist nodes.
 * @param alchemistEnvironment the Alchemist Environment instance.
 * @param node the Alchemist Node instance.
 * @param randomGenerator the random generator of the simulation, to be used by agents for reproducible runs.
 * @param messaging which Alchemist nodes the messages of the hosted agents can reach.
 * @param onShutdown called when a hosted JaKtA node terminates.
 */
class JaktaForAlchemistRuntime<P : Position<P>>(
    val alchemistEnvironment: Environment<Any?, P>,
    override val node: AlchemistNode<Any?>,
    val randomGenerator: RandomGenerator,
    var messaging: Messaging = Messaging.GLOBAL,
    private val onShutdown: (ExecutableNode<*>) -> Unit = {},
) : NodeProperty<Any?> {

    /**
     * The Alchemist node hosting this runtime, i.e. [node], under a name that is not shadowed inside JaKtA DSL blocks.
     */
    val alchemistNode: AlchemistNode<Any?> get() = node

    /**
     * The situated skills of the hosted agents, backed by this Alchemist node.
     */
    val skills: AlchemistSkills<P> = AlchemistSkills { this }

    /**
     * The agents hosted by this Alchemist node.
     */
    val hostedAgents: Set<AgentID> get() = agents.keys.toSet()

    /**
     * The position of this Alchemist node.
     */
    internal val position: Coordinates
        get() = Coordinates(alchemistEnvironment.getPosition(node).coordinates.toList())

    /**
     * [randomGenerator] as a Kotlin [Random].
     */
    internal val random: Random = RandomAdaptor(randomGenerator).asKotlinRandom()

    private val jaktaNodes: MutableList<ExecutableNode<*>> = mutableListOf()
    private val agents: MutableMap<AgentID, HostedAgent> = mutableMapOf()
    private val inbox = UnlimitedChannelQueue<SystemEvent>()
    private val logger = Logger(Logger.config, "Alchemist node ${node.id}")

    /**
     * Configures the runtime to manage the specified Jakta nodes.
     * The initial configuration of nodes can happen only one time at simulation creation time, not later.
     * @param nodes the Jakta [RuntimeNodes].
     */
    fun setInitialJaktaNodes(nodes: RuntimeNodes<*>) {
        check(jaktaNodes.isEmpty()) {
            "Alchemist node ${node.id} already hosts JaKtA nodes: " +
                "declare all of them in a single program, with device { node { ... } node { ... } }"
        }
        jaktaNodes += nodes.nodes
        sendSystemEvents()
        receiveSystemEvents()
    }

    /**
     * Executes one step: handles the system events received since the last step,
     * runs one reasoning step of every hosted agent, and sends the system events they produced.
     */
    fun step() {
        receiveSystemEvents()
        updateBodies() // Alchemist may have moved this node since the last step
        agents.values.toList().forEach { it.step() }
        sendSystemEvents()
    }

    override fun cloneOnNewNode(node: AlchemistNode<Any?>): JaktaForAlchemistRuntime<P> =
        JaktaForAlchemistRuntime(alchemistEnvironment, node, randomGenerator, messaging, onShutdown)

    private fun sendSystemEvents() = jaktaNodes.forEach { jaktaNode ->
        generateSequence { jaktaNode.systemEvents.tryNext() }.forEach(::route)
    }

    private fun receiveSystemEvents() = generateSequence { inbox.tryNext() }.forEach(::handle)

    /**
     * The runtimes of this Alchemist node and of its neighbors.
     */
    internal fun reachableNodes(): Set<JaktaForAlchemistRuntime<*>> =
        (alchemistEnvironment.getNeighborhood(node).neighbors + listOf(node)).runtimes()

    /**
     * Moves this Alchemist node to [position], with all its agents.
     */
    internal fun moveTo(position: Coordinates) {
        alchemistEnvironment.moveNodeToPosition(node, alchemistEnvironment.makePosition(position.values))
        updateBodies()
    }

    // The environment is the source of truth: the Situated bodies of the hosted agents mirror its position
    private fun updateBodies() {
        val here = position
        agents.values.forEach { it.body?.position = here }
    }

    /**
     * Sets the molecule [name] of this Alchemist node to [value], or removes it if [value] is null.
     */
    internal fun setProperty(name: String, value: Any?) {
        val molecule = SimpleMolecule(name)
        when {
            value != null -> node.setConcentration(molecule, value)
            node.contains(molecule) -> node.removeConcentration(molecule)
        }
    }

    // ponytail: every non-message event is broadcast to all Alchemist nodes (O(nodes) per event),
    // address them by NodeID if agent additions/removals become frequent in large simulations.
    private fun route(event: SystemEvent) {
        val recipients = when {
            event is SystemEvent.AgentMessage<*, *> && messaging == Messaging.NEIGHBORHOOD -> reachableNodes()

            // this node might not be in the environment yet, while loading
            else -> (alchemistEnvironment.nodes + listOf(node)).runtimes()
        }
        if (event is SystemEvent.AgentMessage<*, *>) {
            logger.d { "Message ${event.message} reaches ${recipients.size} Alchemist nodes" }
        }
        recipients.forEach { it.inbox.send(event) }
    }

    // Not `nodes + node`: Alchemist nodes are Iterable, it would add the node's reactions
    private fun Iterable<AlchemistNode<Any?>>.runtimes(): Set<JaktaForAlchemistRuntime<*>> =
        mapNotNull { it.asPropertyOrNull<Any?, JaktaForAlchemistRuntime<*>>() }.toSet()

    private fun handle(event: SystemEvent) {
        jaktaNodes.toList().forEach { it.handleExternalEvent(event) }
        when (event) {
            is SystemEvent.AgentAddition<*, *> -> jaktaNodes.find { it.id == event.nodeID }?.let {
                val id = event.executableAgent.id
                val body = it.agents[id] as? Situated
                agents[id] = HostedAgent(it, event.executableAgent, body)
                body?.initialProperties?.forEach(::setProperty)
            }

            is SystemEvent.AgentRemoval -> agents.remove(event.id)

            is SystemEvent.ShutDownNode -> jaktaNodes.find { it.id == event.nodeID }?.let { jaktaNode ->
                jaktaNodes -= jaktaNode
                agents.values.removeAll { it.jaktaNode == jaktaNode }
                logger.i { "JaKtA node ${jaktaNode.id} has been stopped" }
                event.error?.let { throw it } // stops the simulation, reporting the error
                onShutdown(jaktaNode)
            }

            is SystemEvent.AgentMessage<*, *> -> Unit
        }
    }

    private inner class HostedAgent(
        val jaktaNode: ExecutableNode<*>,
        agent: ExecutableAgent<*, *>,
        val body: Situated?,
    ) {
        private val lifecycle = BaseAgentLifecycle(agent)
        private val dispatcher = AlchemistDispatcher(alchemistEnvironment)

        fun step() {
            dispatcher.runDueTasks()
            lifecycle.tryStep(dispatcher)
        }
    }
}
