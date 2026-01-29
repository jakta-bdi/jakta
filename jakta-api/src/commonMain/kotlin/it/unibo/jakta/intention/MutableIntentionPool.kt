package it.unibo.jakta.intention

import it.unibo.jakta.event.AgentEvent


/**
 * A mutable intention pool that allows adding and dropping intentions.
 */
interface MutableIntentionPool : IntentionPool {

    /**
     * Tries to add an intention to the pool.
     * @return true if the intention was added, false if an intention with the same ID already exists.
     */
    fun tryPut(intention: Intention): Boolean

    /**
     * Given an event, it returns an intention to handle it.
     * @param[event] the event to get the intention for.
     * @return a new intention if the event does not reference any existing intention,
     *        or the referenced intention if it exists.
     */
    suspend fun nextIntention(event: AgentEvent.Internal): Intention
    /**
     * Drops the intention with the given ID from the pool.
     * @return true if the intention was found and dropped, false otherwise.
     */
    suspend fun drop(intentionID: IntentionID): Boolean

    /**
     * Executes one step of the intention referenced by this event.
     */
    suspend fun stepIntention(event: AgentEvent.Internal.Step): Unit
}

