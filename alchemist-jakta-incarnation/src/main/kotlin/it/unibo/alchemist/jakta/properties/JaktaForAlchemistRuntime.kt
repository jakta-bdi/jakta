package it.unibo.alchemist.jakta.properties

import co.touchlab.kermit.Logger
import it.unibo.alchemist.jakta.Messaging
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.alchemist.AlchemistDispatcher
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue
import it.unibo.jakta.node.ExecutableNode
import it.unibo.jakta.node.RuntimeNodes
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
        agents.values.toList().forEach { it.step() }
        sendSystemEvents()
    }

    override fun cloneOnNewNode(node: AlchemistNode<Any?>): JaktaForAlchemistRuntime<P> =
        JaktaForAlchemistRuntime(alchemistEnvironment, node, randomGenerator, messaging, onShutdown)

    private fun sendSystemEvents() = jaktaNodes.forEach { jaktaNode ->
        generateSequence { jaktaNode.systemEvents.tryNext() }.forEach(::route)
    }

    private fun receiveSystemEvents() = generateSequence { inbox.tryNext() }.forEach(::handle)

    // ponytail: every non-message event is broadcast to all Alchemist nodes (O(nodes) per event),
    // address them by NodeID if agent additions/removals become frequent in large simulations.
    private fun route(event: SystemEvent) {
        val reachable: Iterable<AlchemistNode<Any?>> = when {
            event is SystemEvent.AgentMessage<*, *> && messaging == Messaging.NEIGHBORHOOD ->
                alchemistEnvironment.getNeighborhood(node).neighbors

            else -> alchemistEnvironment.nodes
        }
        // this node might not be in the environment yet, while loading.
        // Not `reachable + node`: Alchemist nodes are Iterable, it would add the node's reactions
        val recipients = reachable.toMutableSet().apply { add(node) }
        if (event is SystemEvent.AgentMessage<*, *>) {
            logger.d { "Message ${event.message} reaches ${recipients.size} Alchemist nodes" }
        }
        recipients.forEach { it.asPropertyOrNull<Any?, JaktaForAlchemistRuntime<*>>()?.inbox?.send(event) }
    }

    private fun handle(event: SystemEvent) {
        jaktaNodes.toList().forEach { it.handleExternalEvent(event) }
        when (event) {
            is SystemEvent.AgentAddition<*, *> -> jaktaNodes.find { it.id == event.nodeID }?.let {
                agents[event.executableAgent.id] = HostedAgent(it, event.executableAgent)
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

    private inner class HostedAgent(val jaktaNode: ExecutableNode<*>, agent: ExecutableAgent<*, *>) {
        private val lifecycle = BaseAgentLifecycle(agent)
        private val dispatcher = AlchemistDispatcher(alchemistEnvironment)

        fun step() {
            dispatcher.runDueTasks()
            lifecycle.tryStep(dispatcher)
        }
    }
}
