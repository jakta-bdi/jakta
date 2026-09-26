package it.unibo.jakta.intention

import it.unibo.jakta.event.AgentEvent
import kotlinx.coroutines.Job

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
     * @param[currentJob] the job of the current intention,
     * to set as parent of the new intention if a new one is created.
     * @return a new intention if the event does not reference any existing intention,
     *        or the referenced intention if it exists.
     */
    fun nextIntention(event: AgentEvent.Internal, currentJob: Job): Intention

    /**
     * Drops the intention with the given ID, cancelling all its plans: the goals they pursue are intentionally removed,
     * so their removal plans are triggered once each plan has completed its cancellation (e.g. run its finally blocks).
     * The intention leaves the pool once all its plans have completed.
     * @return true if the intention was found and dropped, false otherwise.
     */
    fun drop(intentionID: IntentionID): Boolean

    /**
     * Executes one step of the intention referenced by this event.
     */
    fun stepIntention(event: AgentEvent.Internal.Step): Unit
}
