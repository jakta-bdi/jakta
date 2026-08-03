package it.unibo.alchemist.jakta.properties

import it.unibo.alchemist.jakta.JaktaNode
import it.unibo.alchemist.jakta.actions.JaktaForAlchemistAgent
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.dsl.RuntimeNodes
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.AlchemistNodeConnection
import it.unibo.jakta.node.LocalNodeConnection
import it.unibo.jakta.node.NodeConnection
import it.unibo.jakta.node.NodeSubscription
import kotlinx.coroutines.runBlocking

// TODO this is probably broken now with the latest changes to node runners and management of system events.
//  We need to rework this class to properly handle it.
/** One Alchemist Node may contain more than one Jakta Node.
 * This Alchemist property connects JaKtA meta-model to alchemist representation.
 * @param alchemistEnvironment the Alchemist Environment instance.
 * @param node the Alchemist Node instance.
 */
class JaktaForAlchemistRuntime<P : Position<P>>(
    val alchemistEnvironment: Environment<Any?, P>,
    override val node: AlchemistNode<Any?>,
    private val subscription: NodeSubscription,
) : NodeProperty<Any?> {

    private lateinit var jaktaNodes: RuntimeNodes<JaktaNode<*>>
    private val agentActions: MutableMap<JaktaForAlchemistAgent<P>, JaktaNode<*>> = mutableMapOf()

    /**
     * Configures the runtime to manage the specified Jakta nodes.
     * The initial configuration of nodes can happen only one time at simulation creation time, not later.
     * @param nodes the Jakta [RuntimeNodes].
     */
    fun setInitialJaktaNodes(nodes: RuntimeNodes<JaktaNode<*>>) {
        if (!::jaktaNodes.isInitialized) {
            jaktaNodes = nodes
            jaktaNodes.nodes.forEach { node ->
                var systemEvent: SystemEvent? = node.systemEvents.tryNext()
                while (systemEvent != null) {
                    AlchemistNodeConnection.send(systemEvent)
                    systemEvent = node.systemEvents.tryNext()
                }

                var event = subscription.queue.tryNext()
                while (event != null) {
                    node.handleExternalEvent(event)
                    manageSystemEvent(node, event)
                    event = subscription.queue.tryNext()
                }
            }
        }
    }

    fun stepSystemEvents() {
        jaktaNodes.nodes.forEach { node ->
            // 1. Forwarding node system events to the shared Node Connection
            val nodeSystemEvent =  node.systemEvents.tryNext()
            if (nodeSystemEvent != null) {
                AlchemistNodeConnection.send(nodeSystemEvent)
            }
            // 2. Take next systemEvent to handle from shared Node Connection
            val eventToManage = subscription.queue.tryNext()
            if (eventToManage != null) {
                node.handleExternalEvent(eventToManage)
                manageSystemEvent(node, eventToManage)
            }
        }
    }

    /**
     * @return a list of [Pair] containing the [JaktaForAlchemistAgent] and
     * the associated alchemist Node on which it is being executed.
     */
    fun getAgentActions() = agentActions.toList()

    override fun cloneOnNewNode(node: AlchemistNode<Any?>): JaktaForAlchemistRuntime<P> =
        JaktaForAlchemistRuntime(alchemistEnvironment, node, subscription)

    /**
     * Stores the newly added agent action that is scheduled for execution in the node.
     * @param node the node that will host the agent.
     * @param agent the agent scheduled for the execution in the node.
     */
    private fun addAgentAction(node: JaktaNode<*>, agent: ExecutableAgent<*, *>) {
        val newAgentAction = JaktaForAlchemistAgent<P>(this.node, agent, alchemistEnvironment)
        agentActions += newAgentAction to node
    }

    /**
     * Actions the Runtime will perform on the [JaktaNode] upon receiving the specified [SystemEvent].
     * @param node the node on which the event effect is applied.
     * @param systemEvent the event that is being managed.
     */
    private fun manageSystemEvent(node: JaktaNode<*>, systemEvent: SystemEvent): Unit {
        when (systemEvent) {
            is SystemEvent.AgentAddition<*, *> -> addAgentAction(node, systemEvent.executableAgent)
            is SystemEvent.AgentRemoval -> TODO("Agent Removal not supported for now")
            is SystemEvent.ShutDownNode -> runBlocking {
                subscription.close()
            }
            else -> Unit
        }
    }
}
