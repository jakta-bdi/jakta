package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.event.AgentEvent

/**
 * Represents the shared node in which the agents operate.
 * @param Body The type of body used by agents in this node.
 */
interface Node<Body : Any> {

    /**
     * The unique identifier of the node.
     */
    val id: NodeID

    /**
     * A map of agents currently present in the node,
     * where the key is the unique identifier of the agent and the value is the body of the agent.
     */
    val agents: Map<AgentID, Body>

    /**
     * Publishes an external [event] that is delivered to all agents
     * reachable by that node that satisfy the [filterFunction].
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     */
    fun publishEvent(event: AgentEvent.External, filterFunction: Node<Body>.(Body) -> Boolean = { true })

    /**
     * Adds a new agent to the node based on the provided [agentFactory].
     * @param agentFactory A function that creates an agent specification based on the node.
     */
    fun addAgent(agentFactory: (Node<Body>) -> AgentSpecification<*, *, Body>, nodeID: NodeID = this.id)

    /**
     * Removes an agent from the mas based on its [id].
     * @param id The unique identifier of the agent to be removed.
     */
    fun removeAgent(id: AgentID)

    /**
     * Terminates a node, effectively shutting down all agents and stopping any ongoing processes within the node.
     * @param error An optional exception that caused the node to terminate.
     * @param nodeID The unique identifier of the node to be terminated. If not provided, the current node's ID is used.
     */
    fun terminateNode(error: Throwable? = null, nodeID: NodeID = this.id)

    /**
     * Retrieves the unique identifier of an agent in this node based on its body.
     * @param body The body of the agent for which to retrieve the identifier.
     */
    fun getAgentIDfromBody(body: Body): AgentID? = agents.entries.find { it.value == body }?.key
}
