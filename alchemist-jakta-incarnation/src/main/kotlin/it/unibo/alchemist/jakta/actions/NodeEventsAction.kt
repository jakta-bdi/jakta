package it.unibo.alchemist.jakta.actions

import it.unibo.alchemist.jakta.properties.JaktaForAlchemistRuntime
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node as AlchemistNode
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.alchemist.AlchemistDispatcher
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.node.JaktaForAlchemistNode
import it.unibo.jakta.node.NodeNetwork
import kotlin.collections.plusAssign

class NodeEventsAction <P : Position<P>>(
    private val alchemistNode: AlchemistNode<Any?>,
    private val alchemistEnvironment: Environment<Any?, P>,
    private val jaktaNode: JaktaForAlchemistNode<*>,
) : AbstractAction<Any?>(alchemistNode) {

    override fun getContext(): Context = Context.LOCAL

    override fun cloneAction(node: AlchemistNode<Any?>, reaction: Reaction<Any?>): Action<Any?> = NodeEventsAction(
        alchemistNode,
        alchemistEnvironment,
        jaktaNode
    )

    override fun execute() {
        // 1. Forwarding node system events to the shared Node Connection
        val nodeSystemEvent =  jaktaNode.systemEvents.tryNext()
        if (nodeSystemEvent != null) {
            NodeNetwork.send(nodeSystemEvent)
        }
        // 2. Take next systemEvent to handle from shared Node Connection
        val eventToManage = jaktaNode.subscription.queue.tryNext()
        if (eventToManage != null) {
            jaktaNode.handleExternalEvent(eventToManage)

            alchemistNode.properties
            .filterIsInstance<JaktaForAlchemistRuntime<P>>()
            .firstOrNull()
            ?.manageSystemEvent(jaktaNode, eventToManage)
        }
    }
}
