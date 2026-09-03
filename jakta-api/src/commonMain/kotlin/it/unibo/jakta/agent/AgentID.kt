package it.unibo.jakta.agent

/**
 * Unique identifier for an [Agent].
 */
interface AgentID {
    /**
     * A print-friendly id for the agent.
     */
    val displayName: String
}
