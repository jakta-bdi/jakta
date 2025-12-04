package it.unibo.jakta.intention

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.Event
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

/**
 * Represents a pool of intentions managed by an agent.
 */
interface IntentionPool {
    /**
     * Given an event, it returns an intention to handle it.
     * @param[event] the event to get the intention for.
     * @return a new intention if the event does not reference any existing intention,
     *        or the referenced intention if it exists.
     */
    suspend fun nextIntention(event: Event.Internal): Intention

    /**
     * Returns the set of intentions currently in the pool.
     */
    fun getIntentionsSet(): Set<Intention>
}

/**
 * An intention pool that allows adding intentions.
 */
interface AddableIntentionPool : IntentionPool {
    /**
     * Tries to add an intention to the pool.
     * @return true if the intention was added, false if an intention with the same ID already exists.
     */
    fun tryPut(intention: Intention): Boolean
}

/**
 * A mutable intention pool that allows adding and dropping intentions.
 */
interface MutableIntentionPool : AddableIntentionPool {
    /**
     * Drops the intention with the given ID from the pool.
     * @return true if the intention was found and dropped, false otherwise.
     */
    suspend fun drop(intentionID: IntentionID): Boolean

    /**
     * Executes one step of the intention referenced by this event.
     */
    suspend fun stepIntention(event: Event.Internal.Step): Unit
}

/**
 * Implementation of a mutable intention pool.
 * @param events the channel to send internal events when intentions are ready to step.
 */
class MutableIntentionPoolImpl(val events: SendChannel<Event.Internal.Step>) : MutableIntentionPool {
    private val log =
        Logger(
            Logger.config,
            "IntentionPool", // TODO differentiate between multiple agents e.g. with AgentID
        )

    /** List of intentions currently managed by the agent. **/
    private val intentions: MutableSet<Intention> = mutableSetOf()

    // TODO(This needs to be invoked by someone)
    override suspend fun drop(intentionID: IntentionID): Boolean = intentions.find { it.id == intentionID }?.let {
        it.job.cancelAndJoin() // Cancel the job associated to the intention
        intentions.remove(it)
    } ?: false

    override fun tryPut(intention: Intention): Boolean = intentions.add(intention)

    override suspend fun nextIntention(event: Event.Internal): Intention {
        val nextIntention =
            event.intention?.let {
                // If the referenced intention exists, use its context
                intentions.find { intention -> intention == event.intention } ?: run {
                    // If the referenced intention does not exist, create a new one with that ID
                    // This is useful for debugging purposes, as it allows to name intentions
                    it
                }
            } ?: run {
                val intentionJob = Job(currentCoroutineContext().job)
                val newIntention = _root_ide_package_.it.unibo.jakta.intention.Intention.Key(job = intentionJob)
                newIntention.onReadyToStep(::onIntentionReadyToStep)
                // This is removing the intention if for some reason the job is manually completed
                intentionJob.invokeOnCompletion { intentions.remove(newIntention) }
                newIntention
            }

        tryPut(nextIntention)
        return nextIntention
    }

    override suspend fun stepIntention(event: Event.Internal.Step) {
        log.d { "Stepping intention ${event.intention.id.id}" }
        intentions.find { it == event.intention }?.step() ?: run {
            log.e { "Intention ${event.intention.id.id} not found" }
        }
    }

    override fun getIntentionsSet(): Set<Intention> = setOf(*intentions.toTypedArray())

    private fun onIntentionReadyToStep(intention: Intention) {
        log.d { "Intention ${intention.id.id} is ready to step" }
        events.trySend(Event.Internal.Step(intention))
    }
}
