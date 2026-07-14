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

    private lateinit var jaktaNodes: RuntimeNodes<JaktaNode<*>>
    private val agentActions: MutableMap<JaktaForAlchemistAgent<P>, JaktaNode<*>> = mutableMapOf()

    private fun addAgentAction(node: JaktaNode<*>, agent: ExecutableAgent<*, *>) {
        val newAgentAction = JaktaForAlchemistAgent<P>(
            this.node,
            agent,
            alchemistEnvironment,
        )
        agentActions += newAgentAction to node
    }

    /**
     * Configures the runtime to manage the specified Jakta nodes.
     * The initial configuration of nodes can happen only one time at simulation creation time, not later.
     * @param nodes the Jakta [RuntimeNodes].
     */
    fun setInitialJaktaNodes(nodes: RuntimeNodes<JaktaNode<*>>) {
        if (!::jaktaNodes.isInitialized) {
            jaktaNodes = nodes

            // Jakta Nodes System Events management (
            jaktaNodes.nodes.forEach { node ->
                val systemEvents: EventStream<SystemEvent> = node.systemEvents
                var event: SystemEvent? = systemEvents.tryNext()
                while (event != null) {
                    when (event) {
                        is SystemEvent.AgentAddition<*, *> -> addAgentAction(node, event.executableAgent)
                        is SystemEvent.AgentRemoval -> TODO("Not supported for now")
                        is SystemEvent.ShutDownNode -> TODO("Not supported for now")
                        is SystemEvent.AgentMessage<*, *> -> TODO("Not supported for now")
                    }
                    event = systemEvents.tryNext()
                }
            }
        }
    }

    /**
     * @return a list of [Pair] containing the [JaktaForAlchemistAgent] and
     * the associated alchemist Node on which it is being executed.
     */
    fun getAgentActions() = agentActions.toList()

    override fun cloneOnNewNode(node: AlchemistNode<Any?>): JaktaForAlchemistRuntime<P> =
        JaktaForAlchemistRuntime(alchemistEnvironment, node)
}
