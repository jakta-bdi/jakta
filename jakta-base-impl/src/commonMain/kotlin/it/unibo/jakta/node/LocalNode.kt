package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.BaseAgent
import it.unibo.jakta.event.AgentAdditionEvent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentRemovalEvent
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.ShutDownNodeEvent
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.UnlimitedChannelBus

/**
 * A local implementation of the [it.unibo.jakta.node.Node] interface that manages agents and system events within a single node.
 */
class LocalNode<Body : Any, Skills : Any> : Node<Body, Skills> {

    private val _agents: MutableSet<BaseAgent<*, *, *, Body>> = mutableSetOf()

    override val agents: Map<AgentID, Body>
        get() = _agents.associate { it.id to it.body }

    private val _systemEvents: EventBus<SystemEvent> = UnlimitedChannelBus()

    override val systemEvents: EventStream<SystemEvent>
        get() = _systemEvents

    override fun addAgent(agentSpecification: AgentSpecification<*, *, Skills, Body>) {
        val agent = BaseAgent(agentSpecification)
        _agents += agent
        _systemEvents.send(AgentAdditionEvent(agent))
    }

    override fun removeAgent(id: AgentID) {
        _agents.firstOrNull { it.id == id }?.let {
            _agents.remove(it)
            _systemEvents.send(AgentRemovalEvent(id))
        }
    }

    override fun terminateNode() {
        _systemEvents.send(ShutDownNodeEvent)
    }

    override fun sendEvent(event: AgentEvent.External, filterFunction: Node<Body, Skills>.(Body) -> Boolean) {
        _agents.filter { filterFunction(it.body) }
            .forEach { it.externalInbox.send(event) }
    }
}
