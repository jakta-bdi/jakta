package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.belief.BeliefBaseFactory
import it.unibo.jakta.belief.BeliefRevision
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentEvent.External.Message
import it.unibo.jakta.event.AgentEvent.External.Perception
import it.unibo.jakta.event.AgentEvent.Internal
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.intention.BaseIntentionPool
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType
import kotlin.time.Duration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Default implementation of a [it.unibo.jakta.agent.MutableAgentState].
 */
internal class BaseMutableAgentState<Belief : Any, Goal : Any>(
    initialAgentState: AgentState<Belief, Goal>,
    val internalInbox: EventInbox<Internal>,
    override val id: AgentID,
) : MutableAgentState<Belief, Goal> {

    private val logger: Logger = Logger(
        Logger.config,
        id.displayName,
    )

    override val beliefRevision: BeliefRevision<Belief> = initialAgentState.beliefRevision

    private val beliefBase: BeliefBase<Belief> =
        BeliefBaseFactory.of(internalInbox, initialAgentState.beliefs, beliefRevision)
    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()

    override val mutableIntentionPool: MutableIntentionPool = BaseIntentionPool(internalInbox)
    override val intentions: Set<Intention>
        get() = mutableIntentionPool.getIntentionsSet()

    private val _beliefPlans: MutableList<Plan.Belief<Belief, Goal, *, *>> =
        initialAgentState.beliefPlans.toMutableList()

    override val beliefPlans: List<Plan.Belief<Belief, Goal, *, *>>
        get() = _beliefPlans.toList()

    private val _goalPlans: MutableList<Plan.Goal<Belief, Goal, *, *>> =
        initialAgentState.goalPlans.toMutableList()

    override val goalPlans: List<Plan.Goal<Belief, Goal, *, *>>
        get() = _goalPlans.toList()

    /** A wait in progress: each one is an entry of its own, even when several share the same filter. */
    private class Waiting<T : Any>(val filter: (AgentEvent) -> T?, val resumption: CompletableDeferred<T>) {
        fun resumeOn(event: AgentEvent) {
            filter(event)?.let { resumption.complete(it) }
        }
    }

    private val waiting = mutableListOf<Waiting<*>>()

    private var _perceptionHandler: AgentState<Belief, Goal>.(Perception) -> AgentUpdate<*>? =
        initialAgentState.perceptionHandler

    override val perceptionHandler: AgentState<Belief, Goal>.(Perception) -> AgentUpdate<*>?
        get() = _perceptionHandler

    private var _messageHandler: AgentState<Belief, Goal>.(Message<*>) -> AgentUpdate<*>? =
        initialAgentState.messageHandler

    override val messageHandler: AgentState<Belief, Goal>.(Message<*>) -> AgentUpdate<*>?
        get() = _messageHandler

    override fun setPerceptionHandler(handler: AgentState<Belief, Goal>.(Perception) -> AgentUpdate<*>?) {
        this._perceptionHandler = handler
    }

    override fun setMessageHandler(handler: AgentState<Belief, Goal>.(Message<*>) -> AgentUpdate<*>?) {
        this._messageHandler = handler
    }

    override fun addPlan(plan: Plan.Belief<Belief, Goal, *, *>) {
        this._beliefPlans += plan
    }

    override fun addPlan(plan: Plan.Goal<Belief, Goal, *, *>) {
        this._goalPlans += plan
    }

    @InternalJaktaAPI
    override suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]

        checkNotNull(intention) { "Cannot happen that an achieve invocation comes from a null intention." }

        logger.d { "Achieving $goal. Previous intention $intention" }
        internalInbox.send(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
    }

    override fun alsoAchieve(goal: Goal) {
        internalInbox.send(GoalAddEvent.withNoResult(goal))
    }

    override fun believe(belief: Belief) {
        this.beliefBase.add(belief)
    }

    override fun forget(belief: Belief) {
        this.beliefBase.remove(belief)
    }

    override fun replace(replacements: List<Belief>, scope: (Belief) -> Belief?) {
        this.beliefBase.replace(replacements, scope)
    }

    override fun print(message: String) {
        logger.a { message }
    }

    override suspend fun <T : Any> waitForEvent(eventFilter: (AgentEvent) -> T?, timeout: Duration?): T? {
        val wait = Waiting(eventFilter, CompletableDeferred())
        waiting += wait
        return try {
            if (timeout != null) {
                withTimeoutOrNull(timeout) { wait.resumption.await() }
            } else {
                wait.resumption.await()
            }
        } finally {
            // also when the intention is cancelled (e.g. dropped) while waiting
            waiting -= wait
        }
    }

    /** Resumes the intentions waiting for [event] (see [ExecutableAgent.resumeWaiting]). */
    fun resumeWaiting(event: AgentEvent) {
        // a copy: a resumed intention may stop waiting while the others are checked
        waiting.toList().forEach { it.resumeOn(event) }
    }
}
