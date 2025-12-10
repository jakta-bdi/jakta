package it.unibo.jakta.environment

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Represents the environment in which the agent operates.
 * Provides access to shared resources, including time and randomness.
 * Can be extended to include environment-specific features that should be accessible to all agents within the MAS.
 */
interface Environment {

    /**
     * Gets the current time in milliseconds since the epoch.
     * Can be overridden to provide custom time sources (e.g., simulated time).
     * @return The current time in milliseconds.
     */
    @OptIn(ExperimentalTime::class)
    suspend fun currentTime(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Generates the next random double value between 0.0 and 1.0.
     * Can be overridden to provide custom randomness sources.
     * @return A random double value.
     */
    suspend fun nextRandom(): Double = Random.nextDouble()

    /**
     * Provides a new Random instance seeded with the given seed.
     * Can be overridden to provide custom random generators.
     * @param seed The seed for the random generator.
     * @return A Random instance.
     */
    fun getRandomizer(seed: Int): Random = Random(seed)
}
