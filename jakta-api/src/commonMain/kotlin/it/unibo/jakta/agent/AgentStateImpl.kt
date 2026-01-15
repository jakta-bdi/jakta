package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.event.Event
import it.unibo.jakta.event.Event.External.Message
import it.unibo.jakta.event.Event.External.Perception
import it.unibo.jakta.event.Event.Internal
import it.unibo.jakta.event.EventReceiver
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.IntentionPool
import it.unibo.jakta.intention.MutableIntentionPool
import it.unibo.jakta.intention.MutableIntentionPoolImpl
import it.unibo.jakta.plan.Plan
import kotlin.reflect.KType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext

internal class AgentImmutableState<Belief: Any, Goal: Any, Skills: Any>(
    override val skills: Skills,
    override val beliefs: Collection<Belief>,
    override val intentionPool: IntentionPool,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>>,
    override val perceptionHandler: (Perception) -> Internal?,
    override val messageHandler: (Message) -> Internal?,
    override val initialGoals: List<Goal>,
): AgentState<Belief, Goal, Skills>

internal class AgentStateImpl<Belief: Any, Goal: Any, Skills: Any>(
    initialAgentState: AgentState<Belief, Goal, Skills>,
    override val internalInbox: EventReceiver<Internal>,
    private val onStop: suspend () -> Unit,
): AgentMutableState<Belief, Goal, Skills> {

    private val logger: Logger =  Logger(
        Logger.config,
        // TODO(Agent Name here)
    )

    private val mutableBeliefPlans: MutableList<Plan.Belief<Belief, Goal, Skills, *, *>> = initialAgentState.beliefPlans.toMutableList()
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Skills, *, *>> = mutableBeliefPlans.toList()

    private val mutableGoalPlans: MutableList<Plan.Goal<Belief, Goal, Skills, *, *>> = initialAgentState.goalPlans.toMutableList()
    override val goalPlans: List<Plan.Goal<Belief, Goal, Skills, *, *>> = mutableGoalPlans.toList()

    private var internalPerceptionHandler: (Perception) -> Internal? = initialAgentState.perceptionHandler
    override val perceptionHandler: (Perception) -> Internal? = internalPerceptionHandler

    private var internalMessageHandler: (Message) -> Internal? = initialAgentState.messageHandler
    override val messageHandler: (Message) -> Internal? = internalMessageHandler

    override val initialGoals: List<Goal> = initialAgentState.initialGoals

    // TODO(Is this mutable?)
    override val skills: Skills = initialAgentState.skills

    override val mutableIntentionPool: MutableIntentionPool = MutableIntentionPoolImpl(internalInbox)
    override val intentionPool: IntentionPool = mutableIntentionPool

    private val beliefBase: BeliefBase<Belief> = BeliefBase.of(internalInbox, initialAgentState.beliefs)
    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()

    init {
        initialAgentState.initialGoals.forEach { alsoAchieve(it) }
    }

    override fun setPerceptionHandler(handler: (Event.External.Perception) -> Event.Internal?) {
        this.internalPerceptionHandler = handler
    }

    override fun setMessageHandler(handler: (Event.External.Message) -> Event.Internal?) {
        this.internalMessageHandler = handler
    }

    override fun addPlan(plan: Plan.Belief<Belief, Goal, Skills, *, *>) {
        this.mutableBeliefPlans += plan
    }

    override fun addPlan(plan: Plan.Goal<Belief, Goal, Skills, *, *>) {
        this.mutableGoalPlans += plan
    }

    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]

        check(intention != null) { "Cannot happen that an achieve invocation comes from a null intention." }

        logger.d { "Achieving $goal. Previous intention $intention" }
        internalInbox.trySend(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
    }

    override fun alsoAchieve(goal: Goal) {
        internalInbox.trySend(GoalAddEvent.withNoResult(goal))
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
