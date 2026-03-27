package it.unibo.alchemist.jakta.properties

import it.unibo.alchemist.jakta.actions.JaktaForAlchemistAgent
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.jakta.node.Node as JaktaNode
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.BaseAgentLifecycle
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.dsl.RuntimeNodes
import it.unibo.jakta.event.SystemEvent
import kotlin.collections.component1
import kotlin.collections.component2
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

/** One Alchemist Node may contain more than one Jakta Node.
 * This Alchemist property connects JaKtA meta-model to alchemist representation.
 */
class JaktaForAlchemistRuntime<P: Position<P>>(
    val alchemistEnvironment: Environment<Any?, P>,
    override val node: AlchemistNode<Any?>,
): NodeProperty<Any?> {

    private lateinit var jaktaNodes: RuntimeNodes<JaktaNode<*,*>>
    private val agentActions : MutableMap<JaktaNode<*,*>, JaktaForAlchemistAgent<P>> = mutableMapOf()

    private fun addAgentAction(node: JaktaNode<*,*>, agent: ExecutableAgent<*, *, *>) {
        val newAgentAction = JaktaForAlchemistAgent<P>(this.node, agent)
        agentActions += node to newAgentAction
    }

    /**
     * Configures the runtime to manage the specified Jakta nodes.
     * The initial configuration of nodes can happen only one time at simulation creation time, not later.
     * @param nodes the Jakta [RuntimeNodes].
     */
    fun setInitialJaktaNodes(nodes: RuntimeNodes<JaktaNode<*,*>>) {
        if (!::jaktaNodes.isInitialized) {
            jaktaNodes = nodes

            // Jakta Nodes System Events management (
            jaktaNodes.nodes.forEach { node ->
                var event: SystemEvent? = node.systemEvents.tryNext()
                while (event != null) {
                    when (event) {
                        is SystemEvent.AgentAddition<*, *, *> -> addAgentAction(node, event.executableAgent)
                        is SystemEvent.AgentRemoval -> TODO("Not supported for now")
                        is SystemEvent.ShutDownNode -> TODO("Not supported for now")
                    }
                    event = node.systemEvents.tryNext()
                }
            }
        }
    }

    fun getAgentActions() = agentActions.toList()

    override fun cloneOnNewNode(node: AlchemistNode<Any?>): JaktaForAlchemistRuntime<P> =
        JaktaForAlchemistRuntime(alchemistEnvironment, node)

}
