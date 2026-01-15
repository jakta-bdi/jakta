package it.unibo.jakta.agent.basImpl

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.AgentState
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.event.Event.External.Message
import it.unibo.jakta.event.Event.External.Perception
import it.unibo.jakta.event.Event.Internal
import it.unibo.jakta.event.EventInbox
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.intention.MutableIntentionPoolImpl
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext

/**
 * Default implementation of a [MutableAgentState].
 */
internal class BaseMutableAgentState<Belief: Any, Goal: Any, Skills: Any>(
    initialAgentState: AgentState<Belief, Goal, Skills>,
    val internalInbox: EventInbox<Internal>,
    private val onStop: suspend () -> Unit,
): MutableAgentState<Belief, Goal, Skills> {

    private val logger: Logger =  Logger(
        Logger.config,
        // TODO(Agent Name here)
    )

    //TODO BeliefBase should be injected?
    private val beliefBase: BeliefBase<Belief> = BeliefBase.of(internalInbox, initialAgentState.beliefs)
    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()

    //TODO IntentionPool should be injected?
    override val mutableIntentionPool: MutableIntentionPool = MutableIntentionPoolImpl(internalInbox)
    override val intentions: Set<Intention>
        get() = mutableIntentionPool.getIntentionsSet()

    private val _beliefPlans: MutableList<Plan.Belief<Belief, Goal, Skills, *, *>> = initialAgentState.beliefPlans.toMutableList()
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>
        get() = _beliefPlans.toList()

    private val _goalPlans: MutableList<Plan.Goal<Belief, Goal, Skills, *, *>> = initialAgentState.goalPlans.toMutableList()
    override val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>
        get() = _goalPlans.toList()

    private var _perceptionHandler: (Perception) -> Internal? = initialAgentState.perceptionHandler
    override val perceptionHandler: (Perception) -> Internal?
        get() = _perceptionHandler

    private var _messageHandler: (Message) -> Internal? = initialAgentState.messageHandler
    override val messageHandler: (Message) -> Internal?
        get() = _messageHandler

    // TODO(Is this mutable?)
    override val skills: Skills = initialAgentState.skills


    override fun setPerceptionHandler(handler: (Perception) -> Internal?) {
        this._perceptionHandler = handler
    }

    override fun setMessageHandler(handler: (Message) -> Internal?) {
        this._messageHandler = handler
    }

    override fun addPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>) {
        this._beliefPlans += plan
    }

    override fun addPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>) {
        this._goalPlans += plan
    }

    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]

        check(intention != null) { "Cannot happen that an achieve invocation comes from a null intention." }

        logger.d { "Achieving $goal. Previous intention $intention" }
        internalInbox.send(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
    }

    override fun alsoAchieve(goal: Goal) {
        internalInbox.send(GoalAddEvent.withNoResult(goal))
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
