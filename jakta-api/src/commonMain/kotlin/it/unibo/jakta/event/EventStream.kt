package it.unibo.jakta.event

/**
 * Defines an entity that produces [AgentEvent]s.
 */
interface EventStream<E> {
    /**
     * Blocking function that returns the next event to be managed.
     * If none, it suspends the execution until an event is available.
     * @return the next [E] to be parsed.
     */
    suspend fun next() : E
}
