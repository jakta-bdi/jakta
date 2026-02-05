package it.unibo.jakta.environment

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent

/**
 * Represents the shared environment in which the agents operate.
 * @param Body The type of [AgentBody] used by agents in this environment.
 */
//TODO(This will have an additional generic on the Skills, because the runtime defines the skills the agents will have)
interface Runtime<Position: Any, Displacement: Any, Body: AgentBody> {

    //TODO(This will be removed)
    val topology: Topology<Position, Displacement>

    //TODO(This will be removed)
    val agentPositions: Map<Body, Position>

    val systemEvents: EventStream<SystemEvent>

    /**
     * Sends an external [event] to all agents in the environment that satisfy the [filterFunction].
     * Optionally, a [source] body can be specified if the event originates from a specific agent (e.g. sending a message).
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     * @param source The body of the agent sending the event, if applicable.
     */
    fun sendEvent(
        event: AgentEvent.External,
        filterFunction: Runtime<Position, Displacement, Body>.(Body) -> Boolean = { true },
        source: Body? = null
    )

    //TODO(This will not consider positions. The addition of an agent has effect on the SAME runtime.)
    fun <Belief : Any, Goal : Any, Skills : Any> addAgent(
        agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
        position: Position = topology.defaultPosition
    )

    //TODO(The removal of an agent has effect on the SAME runtime.)
    fun removeAgent(id: AgentID)

    //TODO(this will terminate all agents in the same runtime)
    //TODO(How to implement the termination of the mas?)
    fun terminateMAS()

    fun getBodyByAgentID(id: AgentID): Body?

    //TODO(This will be removed)
    fun getNeighbors(body: Body, range: Double): List<Body> {
        val position = agentPositions[body] ?: return emptyList()
        return agentPositions.filter { (otherBody, otherPosition) ->
            otherBody != body && topology.distance(position, otherPosition) <= range
        }.keys.toList()
    }

    //TODO(This will be removed)
    fun getAgentsInRange(position: Position, range: Double): List<Body> {
        return agentPositions.filter { (_, otherPosition) ->
            topology.distance(position, otherPosition) <= range
        }.keys.toList()
    }
}
