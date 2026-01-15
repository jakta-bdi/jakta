package it.unibo.jakta.event

/**
 * Defines an entity that produces [Event]s.
 */
interface EventSource {
    /**
     * Non-blocking function that returns the next [Event] to be managed.
     * If none, it suspends the execution until an event is available.
     * @return the next [Event] to be parsed.
     */
    suspend fun next() : Event
}
