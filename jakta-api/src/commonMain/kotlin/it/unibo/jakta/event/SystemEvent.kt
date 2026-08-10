package it.unibo.jakta.event

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent
import it.unibo.jakta.node.Node
import it.unibo.jakta.node.NodeID

/**
 * Represents a system event that can occur within the agent system.
 * This sealed interface defines different types of system events related to agent management and node operations.
 */
sealed interface SystemEvent {

    /**
     * Generated when a new agent is added to the system.
     */
    interface AgentAddition<Belief : Any, Goal : Any> : SystemEvent {
        /**
         * The [NodeID] of the node to which the agent should be added.
         */
        val nodeID: NodeID

        /**
         * The [ExecutableAgent] that has been added to the system.
         */
        val executableAgent: ExecutableAgent<Belief, Goal>
    }

    /**
     * Generated when an agent is intentionally removed from the system, either by itself or by another agent.
     */
    interface AgentRemoval : SystemEvent {
        /**
         * The [AgentID] of the agent that has been removed from the system.
         */
        val id: AgentID
    }

    /**
     * Generated when the node is intentionally shut down by an agent.
     */
    interface ShutDownNode : SystemEvent {
        /**
         * The [NodeID] of the node that is being shut down.
         */
        val nodeID: NodeID
    }

    /**
     * The wrapper of an agent message that is sent to the node and then delivered
     * to all agents reachable by that node that satisfy the [filterFunction].
     */
    interface AgentMessage<P : Any, Body : Any> : SystemEvent {
        /**
         * The [AgentEvent.External.Message] that is being sent to the node.
         */
        val message: AgentEvent.External.Message<P>

        /**
         * The function that determines the conditions under which an agent in the receiving node
         * should receive the message.
         */
        val filterFunction: Node<Body>.(Body) -> Boolean
    }
}
