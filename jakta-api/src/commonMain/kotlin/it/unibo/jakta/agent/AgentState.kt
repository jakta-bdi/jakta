package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.event.Event
import it.unibo.jakta.event.Event.External
import it.unibo.jakta.event.Event.External.Message
import it.unibo.jakta.event.Event.External.Perception
import it.unibo.jakta.event.Event.Internal
import it.unibo.jakta.event.EventReceiver
import it.unibo.jakta.event.EventSource
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.IntentionPool
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.intention.MutableIntentionPoolImpl
import it.unibo.jakta.plan.GuardScope
import it.unibo.jakta.plan.Plan
import kotlin.collections.plusAssign
import kotlin.collections.remove
import kotlin.reflect.KType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job

interface AgentState<Belief: Any, Goal: Any, Skills: Any> : GuardScope<Belief> {

    /**
     * The [Skills] the agent is allowed to use.
     */
    val skills: Skills

    /**
     * The *beliefs* currently held by the agent.
     */
    override val beliefs: Collection<Belief>

    val initialGoals: List<Goal>

    /**
     * The pool of intentions currently being pursued by the agent.
     */
    val intentionPool: IntentionPool

    /**
     * The plans available to handle belief-related events.
     */
    val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>

    /**
     * The plans available to handle goal-related events.
     */
    val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>

    /**
     * Mapping function which defines how to (optionally) convert a [Event.External.Perception] into a [Event.Internal].
     */
    val perceptionHandler: (Event.External.Perception) -> Event.Internal?

    /**
     * Mapping function which defines how to (optionally) convert a [Event.External.Message] into a [Event.Internal].
     */
    val messageHandler: (Event.External.Message) -> Event.Internal?

}

