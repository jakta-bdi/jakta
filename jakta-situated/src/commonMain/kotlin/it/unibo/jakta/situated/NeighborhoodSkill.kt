package it.unibo.jakta.situated

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.agent.AgentID

/**
 * A skill to know which agents are currently close to an agent, e.g. within its communication range.
 */
interface NeighborhoodSkill {
    /**
     * The other agents currently in the neighborhood of the agent.
     */
    val Agent.neighbors: Set<AgentID>
}

/**
 * The other agents currently in the neighborhood of the agent, given by the [NeighborhoodSkill] in scope.
 */
context(skill: NeighborhoodSkill)
val Agent.neighbors: Set<AgentID>
    get() = with(skill) { neighbors }
