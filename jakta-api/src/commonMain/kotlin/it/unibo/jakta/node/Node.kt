package it.unibo.jakta.node

/**
 * Represents a Multi-Agent System (MAS) composed of agents operating within a shared environment.
 */
interface Node {
    /**
     * Starts the execution of the MAS, running all the agents.
     */
    suspend fun run(): Unit
}
