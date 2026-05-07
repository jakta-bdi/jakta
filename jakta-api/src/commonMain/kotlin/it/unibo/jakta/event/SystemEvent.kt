package it.unibo.jakta.event

import it.unibo.jakta.agent.AgentID
import it.unibo.jakta.agent.ExecutableAgent

/**
 * Represents a system event that can occur within the agent system.
 * This sealed interface defines different types of system events related to agent management and node operations.
 */
sealed interface SystemEvent {

    /**
     * Generated when a new agent is added to the system.
     */
    interface AgentAddition<Belief : Any, Goal : Any, Skills : Any> : SystemEvent {
        /**
         * The [ExecutableAgent] that has been added to the system.
         */
        val executableAgent: ExecutableAgent<Belief, Goal, Skills>
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
    interface ShutDownNode : SystemEvent
}
