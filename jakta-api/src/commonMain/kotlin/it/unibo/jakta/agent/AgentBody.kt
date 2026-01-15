package it.unibo.jakta.agent

/**
 * The unique body of an [Agent], representing what is visible to other agents in the same environment.
 */
interface AgentBody {
    /**
     * The unique identifier of the agent.
     */
    val id: AgentID
}
