package it.unibo.jakta.agent

/**
 * Agent view visible from other agents.
 */
interface AgentBody {
    /**
     * [AgentID] of the agent. It holds the agent name and its unique identifier in the system.
     */
    val id: AgentID
}
