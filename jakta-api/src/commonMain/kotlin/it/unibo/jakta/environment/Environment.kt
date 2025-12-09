package it.unibo.jakta.environment

import it.unibo.jakta.event.Event
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

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

    /**
     * Shares an [Event.External] to agents sharing this environment instance.
     * @param [Event.External] the event that will be possibly notified to agents.
     */
    fun enqueueExternalEvent(event: Event.External)

    /**
     * The environment starts to listen for upcoming [Event.External] and informs agents about them.
     * @param scope the [CoroutineScope] on which the environment will execute its process.
     */
    fun startPerceiving(scope: CoroutineScope)

    /**
     * The environment stops on listening for [Event.External].
     */
    fun stopPerceiving()
}
