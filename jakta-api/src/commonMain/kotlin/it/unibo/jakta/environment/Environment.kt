package it.unibo.jakta.environment

import it.unibo.jakta.agent.Agent
import it.unibo.jakta.event.Event
import kotlinx.coroutines.channels.SendChannel

/**
 * Represents the environment in which the agent operates.
 * Provides access to shared resources, including time and randomness.
 * Can be extended to include environment-specific features that should be accessible to all agents within the MAS.
 */
interface Environment<Belief : Any, Goal : Any>: SendChannel<Event.External> {

    /**
     * The set of agents that are part of the MAS.
     */
    val agents: Set<Agent<Belief, Goal>>

    /**
     * Adds an agent to the environment.
     * @param agent The agent to be added.
     */
    fun addAgent(agent: Agent<Belief, Goal>)

    /**
     * The environment listens for the next [Event.External] and informs agents about it.
     */
    suspend fun processEvent()

// TODO this will be skills

//    /**
//     * Gets the current time in milliseconds since the epoch.
//     * Can be overridden to provide custom time sources (e.g., simulated time).
//     * @return The current time in milliseconds.
//     */
//    @OptIn(ExperimentalTime::class)
//    suspend fun currentTime(): Long = Clock.System.now().toEpochMilliseconds()
//
//    /**
//     * Generates the next random double value between 0.0 and 1.0.
//     * Can be overridden to provide custom randomness sources.
//     * @return A random double value.
//     */
//    suspend fun nextRandom(): Double = Random.nextDouble()
//
//    /**
//     * Provides a new Random instance seeded with the given seed.
//     * Can be overridden to provide custom random generators.
//     * @param seed The seed for the random generator.
//     * @return A Random instance.
//     */
//    fun getRandomizer(seed: Int): Random = Random(seed)

}
