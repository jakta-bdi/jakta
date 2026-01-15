package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventChannel
import it.unibo.jakta.event.EventReceiver
import it.unibo.jakta.event.EventSource
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.event.GoalFailedEvent
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.IntentionDispatcher
import it.unibo.jakta.intention.IntentionPool
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.intention.MutableIntentionPoolImpl
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import kotlin.collections.filter
import kotlin.coroutines.ContinuationInterceptor
import kotlin.reflect.KType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * Default implementation of an [Agent].
 */
open class AgentImpl<Belief : Any, Goal : Any, Skills: Any, Body: AgentBody>(
    override val body: Body,
    val initialState: AgentState<Belief, Goal, Skills>
) : Agent<Belief, Goal, Skills, Body> {
    private val eventChannel: EventChannel<Event> = EventChannel()
    override val agentEvents: EventSource<Event> = eventChannel
    override val inbox: EventReceiver<Event.External> = eventChannel

    override val state: AgentMutableState<Belief, Goal, Skills> = AgentStateImpl(
        initialState,
        eventChannel,
        {Unit}, //TODO(!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!)
    )


}
