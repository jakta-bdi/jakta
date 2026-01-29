package it.unibo.jakta.intention

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.intention.baseImpl.BaseIntention
import it.unibo.jakta.intention.baseImpl.BaseIntentionID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.CoroutineContext.Key
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel

/**
 * Represents an *intention* in the agent.
 * An intention is a logical sequence of actions that are executed by the agent to handle events:
 * the intention can be formed by a stack of plans and sub-plans that are selected at runtime.
 * An intention may suspend its execution between steps, allowing other intentions to run
 * in a cooperative multitasking fashion.
 * The agent schedules the execution of intention, running an atomic *step* of each intention when possible.
 */
interface Intention : CoroutineContext.Element {
    /**
     * Unique identifier of the intention.
     */
    val id: IntentionID

    /**
     * The [Job] of the intention, used to manage the lifecycle of the coroutines
     * associated with the intention. The job is passed as the parent job of all coroutines launched
     * as part of the intention. If the job is cancelled, all coroutines of the intention are also cancelled.
     */
    val job: Job

    /**
     * Channel of functions representing the continuation of the intention.
     * When the intention resumes from a suspension point, the continuation is sent to this channel.
     * This is used as a blocking queue of continuations to be executed when the intention is stepped.
     */
    val continuations: EventInbox<() -> Unit>

    /**
     * Executes one step of the intention, i.e. executes one continuation from the channel, if available.
     * If no continuation is available, the method does nothing.
     */
    suspend fun step(): Unit

    /**
     * Registers a callback to be invoked when the intention is ready to step.
     */
    fun onReadyToStep(callback: (Intention) -> Unit): Unit

    /**
     * Enqueues a continuation to be executed when the intention is stepped.
     * @param continuation the continuation function to enqueue.
     */
    fun enqueue(continuation: () -> Unit): Unit

    /**
     * Key object for the Intention CoroutineContext element.
     */
    companion object Key : CoroutineContext.Key<Intention>
}

