package it.unibo.jakta.environment

import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.event.Event

/**
 * Represents the shared environment in which the positions operate.
 * @param Body The type of [AgentBody] used by positions in this environment.
 */
interface Environment<Position: Any, Displacement: Any, Body: AgentBody> {

    val topology: Topology<Position, Displacement>

    val positions: Map<Body, Position>

    /**
     * Sends an external [event] to all positions in the environment that satisfy the [filterFunction].
     * Optionally, a [source] body can be specified if the event originates from a specific agent (e.g. sending a message).
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     * @param source The body of the agent sending the event, if applicable.
     */
    fun sendEvent(
        event: Event.External,
        filterFunction: Environment<Position, Displacement, Body>.(Body) -> Boolean = { true },
        source: Body? = null
    )

    fun <Belief: Any, Goal: Any, Skills: Any> addAgent(
        agentSpecification: AgentSpecification<Belief, Goal, Skills, Body>,
        position: Position
    )

    fun removeAgent(body: Body)

    fun getNeighbors(body: Body, range: Double): List<Body> {
        val position = positions[body] ?: return emptyList()
        return positions.filter { (otherBody, otherPosition) ->
            otherBody != body && topology.distance(position, otherPosition) <= range
        }.keys.toList()
    }

    fun getAgentsInRange(position: Position, range: Double): List<Body> {
        return positions.filter { (_, otherPosition) ->
            topology.distance(position, otherPosition) <= range
        }.keys.toList()
    }

}
