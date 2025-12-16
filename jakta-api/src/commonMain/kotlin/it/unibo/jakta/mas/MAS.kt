package it.unibo.jakta.mas

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.environment.Environment

/**
 * Represents a Multi-Agent System (MAS) composed of agents operating within a shared environment.
 */
interface MAS<Belief : Any, Goal : Any, Env : Environment> {
    /**
     * The shared environment in which the agents operate.
     */
    val environment: Env

    /**
     * Starts the execution of the MAS.
     */
    suspend fun run(): Unit
}
