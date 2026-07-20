package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.InternalJaktaAPI
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.belief.BeliefBaseFactory
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

    private val beliefBase: BeliefBase<Belief> = BeliefBaseFactory.of(internalInbox, initialAgentState.beliefs)
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

    override val waitEventFilters: MutableMap<(AgentEvent) -> Any?, CompletableDeferred<*>> =
        mutableMapOf()

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

    override fun print(message: String) {
        logger.a { message }
    }

    //TODO check this function I am not sure..
    override suspend fun <T : Any> wait(eventFilter: (AgentEvent) -> T?, timeout: Duration?) : T? {
        val deferred = CompletableDeferred<T>()
        this.waitEventFilters[eventFilter] = deferred
        return timeout?.let {
            withTimeoutOrNull(it) {
                deferred.await()
            }
        }?.run {
            deferred.await()
        }
    }
}
