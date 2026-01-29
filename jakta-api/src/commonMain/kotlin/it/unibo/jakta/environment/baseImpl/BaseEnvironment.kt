package it.unibo.jakta.environment.baseImpl

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.basImpl.BaseAgent
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.environment.Topology
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventBus
import it.unibo.jakta.event.baseImpl.UnlimitedChannelBus
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent
import it.unibo.jakta.event.baseImpl.AgentAdditionEvent
import it.unibo.jakta.event.baseImpl.AgentRemovalEvent
import it.unibo.jakta.event.baseImpl.ShutDownMASEvent

class BaseEnvironment<Position: Any, Displacement: Any, Body: AgentBody>(
    override val topology: Topology<Position, Displacement>,
) : Environment<Position, Displacement, Body> {

    private val _agents: MutableMap<BaseAgent<*, *, *, Body>, Position> = mutableMapOf()

    override val agentPositions: Map<Body, Position>
        get() = _agents.map{ (agent, position) -> agent.body to position }.toMap()


    private val _systemEvents: EventBus<SystemEvent> = UnlimitedChannelBus()

    override val systemEvents : EventStream<SystemEvent>
        get() = _systemEvents


    override fun <Belief : Any, Goal : Any, Skills : Any> addAgent(
        agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
        position: Position
    ) {
        val agent = BaseAgent(agentSpecification)
        _agents[agent] = position
        _systemEvents.send(AgentAdditionEvent(agent))
    }

    override fun removeAgent(id: AgentID) {
        _agents.keys.find { it.id == id }?.let {
            _agents.remove(it)
            _systemEvents.send(AgentRemovalEvent(id))
        }
    }

    override fun terminateMAS() {
        _systemEvents.send(ShutDownMASEvent)
    }

    override fun getBodyByAgentID(id: AgentID): Body? =
        _agents.keys.firstOrNull{it.id == id}?.body

    override fun sendEvent(
        event: AgentEvent.External,
        filterFunction: Environment<Position, Displacement, Body>.(Body) -> Boolean,
        source: Body?
    ) {
        _agents.keys
            .filterNot { source != null && it.body == source }
            .filter { filterFunction(it.body) }
            .forEach { it.externalInbox.send(event) }
    }

}
