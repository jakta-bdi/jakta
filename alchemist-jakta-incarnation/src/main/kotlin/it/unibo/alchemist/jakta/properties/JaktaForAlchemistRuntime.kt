package it.unibo.alchemist.jakta.properties

import it.unibo.alchemist.jakta.actions.JaktaAgentAction
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.JaktaForAlchemistNode
import it.unibo.jakta.node.NodeNetwork
import it.unibo.jakta.node.NodeSubscription
import it.unibo.jakta.node.RuntimeNodes
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
) : NodeProperty<Any?> {

    private lateinit var jaktaNodes: RuntimeNodes<JaktaForAlchemistNode<*>>
    private val agentActions: MutableMap<JaktaAgentAction<P>, JaktaForAlchemistNode<*>> = mutableMapOf()

    /**
     * Configures the runtime to manage the specified Jakta nodes.
     * The initial configuration of nodes can happen only one time at simulation creation time, not later.
     * @param nodes the Jakta [RuntimeNodes].
     */
    fun setInitialJaktaNodes(nodes: RuntimeNodes<JaktaForAlchemistNode<*>>) {
        if (!::jaktaNodes.isInitialized) {
            jaktaNodes = nodes
            jaktaNodes.nodes.forEach { node ->
                var systemEvent: SystemEvent? = node.systemEvents.tryNext()
                while (systemEvent != null) {
                    NodeNetwork.send(systemEvent)
                    systemEvent = node.systemEvents.tryNext()
                }

                var event = node.subscription.queue.tryNext()
                while (event != null) {
                    node.handleExternalEvent(event)
                    manageSystemEvent(node, event)
                    event = node.subscription.queue.tryNext()
                }
            }
        }
    }

    /**
     * @return a list of [Pair] containing the [JaktaAgentAction] and
     * the associated alchemist Node on which it is being executed.
     */
    fun getAgentActions() = agentActions.toList()

    override fun cloneOnNewNode(node: AlchemistNode<Any?>): JaktaForAlchemistRuntime<P> =
        JaktaForAlchemistRuntime(alchemistEnvironment, node)

    /**
     * Stores the newly added agent action that is scheduled for execution in the node.
     * @param node the node that will host the agent.
     * @param agent the agent scheduled for the execution in the node.
     */
    private fun addAgentAction(node: JaktaForAlchemistNode<*>, agent: ExecutableAgent<*, *>) {
        val newAgentAction = JaktaAgentAction<P>(this.node, agent, alchemistEnvironment)
        agentActions += newAgentAction to node
    }

    /**
     * Actions the Runtime will perform on the [JaktaNode] upon receiving the specified [SystemEvent].
     * @param node the node on which the event effect is applied.
     * @param systemEvent the event that is being managed.
     */
    fun manageSystemEvent(node: JaktaForAlchemistNode<*>, systemEvent: SystemEvent): Unit {
        when (systemEvent) {
            is SystemEvent.AgentAddition<*, *> -> if (systemEvent.nodeID == node.id) {
                addAgentAction(node, systemEvent.executableAgent)
            }
            is SystemEvent.AgentRemoval -> TODO("Agent Removal not supported for now")
            is SystemEvent.ShutDownNode -> if (systemEvent.nodeID == node.id) {
                runBlocking {
                    node.subscription.close()
                }
            }
            else -> Unit
        }
    }
}
