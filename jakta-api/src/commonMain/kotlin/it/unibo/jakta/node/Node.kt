package it.unibo.jakta.node

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.agent.RuntimeAgent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent

// TODO right now all operations on a node are local to the node itself.
//  we may need to revisit the semantics of the operations when moving to a system of multiple nodes.
/**
 * Represents the shared node in which the agents operate.
 * @param Body The type of body used by agents in this node.
 */
interface Node<Body : Any, Skills : Any> {

    /**
     * A map of agents currently present in the node,
     * where the key is the unique identifier of the agent and the value is the body of the agent.
     */
    val agents: Map<AgentID, Body>

    /**
     * An event stream that emits system events related to the node.
     */
    val systemEvents: EventStream<SystemEvent>

    /**
     * Sends an external [event] to all agents in the node that satisfy the [filterFunction].
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     */

    fun sendEvent(event: AgentEvent.External, filterFunction: Node<Body, Skills>.(Body) -> Boolean = { true })

    /**
     * Adds a new agent to the node based on the provided [agentSpecification].
     * @param agentSpecification The specification of the agent to be added.
     */
    fun addAgent(agentSpecification: AgentSpecification<*, *, Skills, Body>)

    /**
     * Removes an agent from the node based on its [id].
     * @param id The unique identifier of the agent to be removed.
     */
    fun removeAgent(id: AgentID)

    /**
     * Terminates the node, effectively shutting down all agents and stopping any ongoing processes within the node.
     */
    fun terminateNode()
}
