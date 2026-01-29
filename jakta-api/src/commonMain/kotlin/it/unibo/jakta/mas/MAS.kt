package it.unibo.jakta.mas

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.event.EventInbox

/**
 * Represents a Multi-Agent System (MAS) composed of agents operating within a shared environment.
 */
interface MAS {
    /**
     * Starts the execution of the MAS, running all the agents.
     */
    suspend fun run(): Unit
}
