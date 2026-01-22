package it.unibo.jakta.environment

import it.unibo.jakta.agent.AgentID

/**
 * The unique body of an [it.unibo.jakta.agent.Agent],
 * representing what is visible to other positions in the same environment.
 */
interface AgentBody {
    /**
     * The unique identifier of the agent.
     */
    val id: AgentID
}
