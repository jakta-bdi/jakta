package it.unibo.jakta.node.baseImpl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.RuntimeAgent
import it.unibo.jakta.agent.basImpl.BaseAgent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.baseImpl.AgentAdditionEvent
import it.unibo.jakta.event.baseImpl.AgentRemovalEvent
import it.unibo.jakta.event.baseImpl.ShutDownNodeEvent
import it.unibo.jakta.event.baseImpl.UnlimitedChannelBus
import it.unibo.jakta.node.AgentBody
import it.unibo.jakta.node.Node

class LocalNode<Body : AgentBody, Skills : Any> : Node<Body, Skills> {

    private val _agents: MutableSet<BaseAgent<*, *, *, Body>> = mutableSetOf()

    override val agents: Set<RuntimeAgent<Body>>
        get() = _agents.toSet()

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

    override fun getBodyByAgentID(id: AgentID): Body? = _agents.firstOrNull { it.id == id }?.body

    override fun sendEvent(
        event: AgentEvent.External,
        filterFunction: Node<Body, Skills>.(Body) -> Boolean,
        source: Body?,
    ) {
        _agents.filterNot { source != null && it.body == source }
            .filter { filterFunction(it.body) }
            .forEach { it.externalInbox.send(event) }
    }
}
