package it.unibo.jakta.node

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.AgentSpecification
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventStream
import it.unibo.jakta.event.SystemEvent

/**
 * Represents the shared node in which the agents operate.
 * @param Body The type of [AgentBody] used by agents in this node.
 */
interface Node<Body: AgentBody, Skills: Any> {

    val agents: Set<Body>

    val systemEvents: EventStream<SystemEvent>

    /**
     * Sends an external [event] to all agents in the node that satisfy the [filterFunction].
     * Optionally, a [source] body can be specified if the event originates from a specific agent (e.g. sending a message).
     * @param event The external event to be sent.
     * @param filterFunction A function that determines the conditions under which an agent should receive the event.
     * @param source The body of the agent sending the event, if applicable.
     */
    //TODO maybe this won't be limited to work on the same node, and different implementations may change how it works
    // or is it better to add messages
    fun sendEvent(
        event: AgentEvent.External,
        filterFunction: Node<Body, Skills>.(Body) -> Boolean = { true },
        source: Body? = null
    )

    //TODO(The addition of an agent has effect on the SAME node.)
    fun addAgent(
        agentSpecification: AgentSpecification<*, *, Skills, Body>,
    )

    //TODO(The removal of an agent has effect on the SAME node.)
    fun removeAgent(id: AgentID)

    //TODO(this will terminate all agents in the same node)
    //TODO(How to implement the termination of the mas?)
    fun terminateNode()

    fun getBodyByAgentID(id: AgentID): Body?
}
