package it.unibo.jakta.intention.baseImpl

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.IntentionID
import it.unibo.jakta.intention.MutableIntentionPool
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

/**
 * Implementation of a mutable intention pool.
 * @param events the channel to send internal events when intentions are ready to step.
 */
class BaseIntentionPoolImpl(val eventInbox: EventInbox<AgentEvent.Internal.Step>) : MutableIntentionPool {
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

    override suspend fun nextIntention(event: AgentEvent.Internal): Intention {
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
                val newIntention = BaseIntention(intentionJob)
                newIntention.onReadyToStep(::onIntentionReadyToStep)
                // This is removing the intention if for some reason the job is manually completed
                intentionJob.invokeOnCompletion { intentions.remove(newIntention) }
                newIntention
            }

        tryPut(nextIntention)
        return nextIntention
    }

    override suspend fun stepIntention(event: AgentEvent.Internal.Step) {
        log.d { "Stepping intention ${event.intention.id.displayId}" }
        intentions.find { it == event.intention }?.step() ?: run {
            log.e { "Intention ${event.intention.id.displayId} not found" }
        }
    }

    override fun getIntentionsSet(): Set<Intention> = setOf(*intentions.toTypedArray())

    private fun onIntentionReadyToStep(intention: Intention) {
        log.d { "Intention ${intention.id.displayId} is ready to step" }
        eventInbox.send(AgentEvent.Internal.Step(intention))
    }
}
