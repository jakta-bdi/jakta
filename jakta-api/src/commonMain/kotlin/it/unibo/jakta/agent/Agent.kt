package it.unibo.jakta.agent

/**
 * Top-level interface representing an agent as an entity with a unique identifier.
 */
interface Agent {
    /**
     * The unique identifier of the agent.
     */
    val id: AgentID
}
