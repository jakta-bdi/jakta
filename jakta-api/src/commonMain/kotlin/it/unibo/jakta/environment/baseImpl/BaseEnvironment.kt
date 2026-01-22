package it.unibo.jakta.environment.baseImpl

import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.EnvironmentAgent
import it.unibo.jakta.agent.basImpl.BaseAgent
import it.unibo.jakta.environment.AgentBody
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.environment.Topology
import it.unibo.jakta.event.Event

class BaseEnvironment<Position: Any, Displacement: Any, Body: AgentBody>(
    override val topology: Topology<Position, Displacement>,
) : Environment<Position, Displacement, Body> {

    private val _positions: MutableMap<EnvironmentAgent<Body>, Position> = mutableMapOf()

    override val positions: Map<Body, Position>
        get() = _positions.map{ (agent, position) -> agent.body to position }.toMap()


    //TODO notify the MAS when an agent is added or removed
    override fun <Belief : Any, Goal : Any, Skills : Any> addAgent(
        agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
        position: Position
    ) {
        val agent = BaseAgent(agentSpecification)
        _positions[agent] = position
        //TODO notify mas and start the agent execution
    }

    override fun removeAgent(body: Body) {
        val agentToRemove = _positions.keys.find { it.body == body }
        if (agentToRemove != null) {
            _positions.remove(agentToRemove)
        }
    }


    override fun sendEvent(
        event: Event.External,
        filterFunction: Environment<Position, Displacement, Body>.(Body) -> Boolean,
        source: Body?
    ) {
        _positions.keys
            .filterNot { source != null && it.body == source }
            .filter { filterFunction(it.body) }
            .forEach { it.externalInbox.send(event) }
    }


}
