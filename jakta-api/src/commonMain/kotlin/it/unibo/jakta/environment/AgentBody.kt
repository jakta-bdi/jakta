package it.unibo.jakta.environment

import it.unibo.jakta.agent.AgentID

/**
 * The unique body of an [it.unibo.jakta.agent.Agent],
 * representing what is visible to other agents in the same environment.
 */
interface AgentBody {
    //TODO how do I make the AgentBody ID expose the same ID of the Agent?
    // and in general link the body to the agent e.g. state?
    // or is this a responsibility of who implements the AgentBody?
}
