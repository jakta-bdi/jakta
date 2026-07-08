package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent

/**
 * Represents the shared node in which the agents operate.
 * @param Body The type of body used by agents in this node.
 */
interface Node<Body : Any> {

    /**
     * A map of agents currently present in the node,
     * where the key is the unique identifier of the agent and the value is the body of the agent.
     */
    val agents: Map<AgentID, Body>

    /**
     * Sends an external [event] to all agents in the node that satisfy the [filterFunction].
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     */
    fun sendEvent(event: AgentEvent.External, filterFunction: Node<Body>.(Body) -> Boolean = { true })

    /**
     * Adds a new agent to the node based on the provided [agentSpecification].
     * @param agentSpecification The specification of the agent to be added.
     */
    fun addAgent(agentSpecification: AgentSpecification<*, *, Body>)

    /**
     * Removes an agent from the node based on its [id].
     * @param id The unique identifier of the agent to be removed.
     */
    fun removeAgent(id: AgentID)

    /**
     * Terminates the node, effectively shutting down all agents and stopping any ongoing processes within the node.
     */
    fun terminateNode()

    /**
     * Retrieves the unique identifier of an agent based on its body.
     * @param body The body of the agent for which to retrieve the identifier.
     */
    fun getAgentIDfromBody(body: Body): AgentID? = agents.entries.find { it.value == body }?.key
}

/**
 * Represents a node that can be executed and managed, providing an event stream for system events.
 * @param Body The type of body used by agents in this node.
 */
interface ExecutableNode<Body : Any> : Node<Body> {
    /**
     * An event stream that emits system events related to the node.
     */
    val systemEvents: EventStream<SystemEvent>
}
