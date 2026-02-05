package it.unibo.jakta.agent

/**
 * Unique identifier for an [Agent].
 */
interface AgentID {
    /**
     * The id serialized as a [String].
     */
    val displayName: String
}
