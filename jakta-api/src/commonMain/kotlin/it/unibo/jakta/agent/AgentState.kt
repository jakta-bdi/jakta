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
     * The *beliefs* currently held by the agent.
     */
    override val beliefs: Collection<Belief>

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

    /**
     * Logs a message to the agent's output.
     * @param[message] The message to be printed.
     */
    fun print(message: String)
}

internal class AgentStateImpl<Belief: Any, Goal: Any, Skills: Any>(
    private val eventReceiver: EventReceiver,
    private val logger: Logger,
    private val onStop: suspend () -> Unit,
    initialGoals: List<Goal>,
    initialBeliefs: Collection<Belief>,
    override var beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>,
    override var goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>,
    override var perceptionHandler: (Perception) -> Internal?,
    override var messageHandler: (Message) -> Internal?
): AgentState<Belief, Goal, Skills>, AgentMutableState<Belief, Goal, Skills> {

    override val mutableIntentionPool: MutableIntentionPool = MutableIntentionPoolImpl(eventReceiver)
    override val intentionPool: IntentionPool = mutableIntentionPool

    private val beliefBase: BeliefBase<Belief> = BeliefBase.of(eventReceiver, initialBeliefs)

    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()

    init {
        initialGoals.forEach { alsoAchieve(it) }
    }

    override fun setPerceptionHandler(handler: (Event.External.Perception) -> Event.Internal?) {
        this.perceptionHandler = handler
    }

    override fun setMessageHandler(handler: (Event.External.Message) -> Event.Internal?) {
        this.messageHandler = handler
    }

    override fun addPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>) {
        this.beliefPlans += plan
    }

    override fun addPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>) {
        this.goalPlans += plan
    }

    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]

        check(intention != null) { "Cannot happen that an achieve invocation comes from a null intention." }

        logger.d { "Achieving $goal. Previous intention $intention" }
        eventReceiver.trySend(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
    }

    override fun alsoAchieve(goal: Goal) {
        eventReceiver.trySend(GoalAddEvent.withNoResult(goal))
    }

    override suspend fun terminate() = onStop()

    override suspend fun believe(belief: Belief) {
        this.beliefBase.add(belief)
    }

    // TODO should I have also update belief?
    override suspend fun forget(belief: Belief) {
        this.beliefBase.remove(belief)
    }

    override fun print(message: String) {
        logger.a { message }
    }




}
