package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.BaseAgent
import it.unibo.jakta.event.AgentAdditionEvent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentMessageEvent
import it.unibo.jakta.event.AgentRemovalEvent
import it.unibo.jakta.event.EventQueue
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.ShutDownNodeEvent
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelQueue

/**
 * An implementation of the [it.unibo.jakta.node.Node] interface
 * that manages agents and system events within a single isolated node.
 */
open class BaseNode<Body : Any> : ExecutableNode<Body> {

    private val _agents: MutableSet<BaseAgent<*, *, Body>> = mutableSetOf()

    override val id: NodeID = NodeID()

    override val agents: Map<AgentID, Body>
        get() = _agents.associate { it.id to it.body }

    private val _systemEvents: EventQueue<SystemEvent> = UnlimitedChannelQueue()

    override val systemEvents: EventStream<SystemEvent>
        get() = _systemEvents

    override fun addAgent(agentSpecification: AgentSpecification<*, *, Body>) {
        val agent = BaseAgent(agentSpecification)
        _systemEvents.send(AgentAdditionEvent(agent, this.id))
    }

    override fun removeAgent(id: AgentID) {
        _systemEvents.send(AgentRemovalEvent(id))
    }

    override fun terminateNode(nodeID: NodeID) {
        _systemEvents.send(ShutDownNodeEvent(nodeID))
    }

    override fun publishEvent(event: AgentEvent.External, filterFunction: Node<Body>.(Body) -> Boolean) = when (event) {
        is AgentEvent.External.Message<*> -> _systemEvents.send(AgentMessageEvent(event, filterFunction))
        is AgentEvent.External.Perception -> deliverEvent(event, filterFunction)
    }

    // TODO check this cast, can I remove it somehow?
    @Suppress("UNCHECKED_CAST")
    override fun handleExternalEvent(event: SystemEvent) {
        when (event) {
            is AgentMessageEvent<*, *> -> {
                deliverEvent(event.message, event.filterFunction as Node<Body>.(Body) -> Boolean)
            }
            is AgentRemovalEvent -> {
                _agents.firstOrNull { it.id == id }?.let {
                    _agents.remove(it)
                }
            }
            is SystemEvent.AgentAddition<*, *> -> {
                val agent = event.executableAgent as BaseAgent<*, *, Body>
                _agents.add(agent)
            }
            else -> Unit
        }
    }

    private fun deliverEvent(event: AgentEvent.External, filterFunction: Node<Body>.(Body) -> Boolean) {
        _agents.filter { filterFunction(it.body) }
            .forEach { it.externalInbox.send(event) }
    }
}
