package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.belief.BeliefBase
import it.unibo.jakta.environment.Environment
import it.unibo.jakta.environment.EnvironmentContext
import it.unibo.jakta.event.Event
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.event.GoalFailedEvent
import it.unibo.jakta.intention.Intention
import it.unibo.jakta.intention.IntentionDispatcher
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
open class AgentImpl<Belief : Any, Goal : Any, Env : Environment>(
    initialBeliefs: Collection<Belief>,
    initialGoals: List<Goal>,
    override val beliefPlans: List<Plan.Belief<Belief, Goal, Env, *, *>>,
    override val goalPlans: List<Plan.Goal<Belief, Goal, Env, *, *>>,
    private val agentID: AgentID = AgentID(),
    private val events: Channel<Event.Internal> = Channel(Channel.UNLIMITED),
) : Agent<Belief, Goal, Env>,
    AgentActions<Belief, Goal>,
    GuardScope<Belief>,
    SendChannel<Event.Internal> by events {
    private val log =
        Logger(
            Logger.config,
            this.name,
        )
    override val name: String
        get() = agentID.displayName

    override val beliefs: Collection<Belief>
        get() = beliefBase.snapshot()

    private val beliefBase: BeliefBase<Belief> = BeliefBase.of(this, initialBeliefs)
    private val intentionPool: MutableIntentionPool =
        MutableIntentionPoolImpl(this)

    init {
        initialGoals.forEach { alsoAchieve(it) }
    }

    override suspend fun step(scope: CoroutineScope) {
        log.i { "waiting for event..." }
        val event = events.receive()
        log.i { "received event: $event" }
        when (event) {
            // TODO per rimuovere questo cast dovrei tipare Event.Internal
            //  con Belief e Goal (si può fare ma è subottimo?)
            is Event.Internal.Belief<*> -> scope.handleBeliefEvent(event as Event.Internal.Belief<Belief>)
            is Event.Internal.Goal<*, *> -> scope.handleGoalEvent(event as Event.Internal.Goal<Goal, Any?>)
            is Event.Internal.Step -> handleStepEvent(event)
        }
    }

    /**
     * Launches plans in the SupervisorScope of the Step.
     * @param event the belief event that triggered the plan execution.
     */
    private suspend fun CoroutineScope.handleBeliefEvent(event: Event.Internal.Belief<Belief>) {
        selectPlan(
            entity = event.belief,
            entityMessage = when (event) {
                is Event.Internal.Belief.Add<Belief> -> "addition of belief"
                is Event.Internal.Belief.Remove<Belief> -> "removal of belief"
            },
            planList = beliefPlans,
            relevantFilter = {
                when (event) {
                    is Event.Internal.Belief.Add<Belief> -> it is Plan.Belief.Addition
                    is Event.Internal.Belief.Remove<Belief> -> it is Plan.Belief.Removal
                } && it.isRelevant(event.belief)
            },
            applicableFilter = {
                it.isApplicable(this@AgentImpl, event.belief)
            },
        )?.let {
            launchPlan(event, event.belief, it)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    // TODO In order to be capable to complete the completion, i had to remove the star projection and put Any?
    //  This requires refactoring of type management
    private suspend fun CoroutineScope.handleGoalEvent(event: it.unibo.jakta.event.Event.Internal.Goal<Goal, Any?>) {
        selectPlan(
            entity = event.goal,
            entityMessage = when (event) {
                is Event.Internal.Goal.Add<Goal, *> -> "addition of goal"
                is Event.Internal.Goal.Remove<Goal, *> -> "removal of goal"
                is Event.Internal.Goal.Failed<Goal, *> -> "failure of goal"
            },
            planList = goalPlans,
            relevantFilter = {
                when (event) {
                    is Event.Internal.Goal.Add<Goal, *> -> it is Plan.Goal.Addition
                    is Event.Internal.Goal.Remove<Goal, *> -> it is Plan.Goal.Removal
                    is Event.Internal.Goal.Failed<Goal, *> -> it is Plan.Goal.Failure
                } && it.isRelevant(event.goal)
            },
            applicableFilter = {
                it.isApplicable(this@AgentImpl, event.goal)
            },
        )?.let {
            launchPlan(event, event.goal, it, event.completion)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    private suspend fun <TriggerEntity : Any> CoroutineScope.launchPlan(
        event: Event.Internal,
        entity: TriggerEntity,
        plan: Plan<Belief, Goal, Env, TriggerEntity, *, *>,
        completion: CompletableDeferred<Any?>? = null, // TODO Check if this Any? can be improved
    ) {
        log.d { "Launching plan $plan for event $event" }
        val environment: Env = currentCoroutineContext()[EnvironmentContext.Key]?.environment as Env
        val intention = intentionPool.nextIntention(event)

        val interceptor =
            currentCoroutineContext()[ContinuationInterceptor] ?: error { "No ContinuationInterceptor in context" }

        launch(IntentionDispatcher(interceptor) + intention + intention.job) {
            // TODO maybe I should not suppress?? But I want to catch ALL exceptions..
            @Suppress("TooGenericExceptionCaught")
            try {
                log.d { "Running plan $plan" }
                val result = plan.run(this@AgentImpl, this@AgentImpl, environment, entity)
                completion?.complete(result)
            } catch (e: Exception) {
                handleFailure(event, e)
            }
        }
        log.d { "Launched plan $plan" }
    }

    private fun <TriggerEntity : Any> selectPlan(
        entity: TriggerEntity,
        entityMessage: String,
        planList: List<Plan<Belief, Goal, Env, TriggerEntity, *, *>>,
        relevantFilter: (Plan<Belief, Goal, Env, TriggerEntity, *, *>) -> Boolean,
        applicableFilter: (Plan<Belief, Goal, Env, TriggerEntity, *, *>) -> Boolean,
    ): Plan<Belief, Goal, Env, TriggerEntity, *, *>? {
        val relevant = planList.filter(relevantFilter)

        if (relevant.isEmpty()) {
            log.w { "No relevant plans for $entityMessage: $entity" }
        }

        val applicable = relevant.filter(applicableFilter)

        if (applicable.isEmpty()) {
            log.w { "No applicable plans for $entityMessage: $entity" }
        }

        return applicable.firstOrNull()?.let {
            log.d { "Selected plan $it for $entityMessage: $entity" }
            it
        } ?: run {
            log.w { "No plan selected for $entityMessage: $entity" }
            null
        }
    }

    // TODO check if this is enough
    // what happens if a belief plan fails?
    private fun handleFailure(event: Event.Internal, e: Exception) {
        when (event) {
            is Event.Internal.Goal.Add<*, *> -> {
                log.d { "Attempting to handle the failure of goal: $event.goal" }
                events.trySend(
                    GoalFailedEvent(
                        event.goal,
                        event.completion,
                        event.intention,
                        event.resultType,
                    ),
                )
            }
            is Event.Internal.Goal.Failed<*, *> -> {
                log.d { "An error occurred when attempting to handle the failure of goal: $event.goal" }
                event.completion?.completeExceptionally(e)
            }
            else -> {
                log.w { "Handling of event $event failed with exception:${e.message}\n${e.stackTraceToString()}" }
            }
        }
    }

    // TODO(Missing implementation for greedy event selection in case Step.intention was removed from intention pool)
    private suspend fun handleStepEvent(event: Event.Internal.Step) {
        log.d { "Handling step event for intention ${event.intention.id.id}" }
        intentionPool.stepIntention(event)
    }

    @Deprecated("Use achieve instead", replaceWith = ReplaceWith("achieve(goal)"), level = DeprecationLevel.ERROR)
    override suspend fun <PlanResult> internalAchieve(goal: Goal, resultType: KType): PlanResult {
        val completion = CompletableDeferred<PlanResult>()
        val intention = currentCoroutineContext()[Intention]

        check(intention != null) { "Cannot happen that an achieve invocation comes from a null intention." }

        log.d { "Achieving $goal. Previous intention $intention" }
        events.trySend(GoalAddEvent(goal, resultType, completion, intention))
        return completion.await() // Blocking the continuation
    }

    override fun print(message: String) {
        log.a { message }
    }

    override fun alsoAchieve(goal: Goal) {
        events.trySend(GoalAddEvent.withNoResult(goal))
    }

    override suspend fun believe(belief: Belief) {
        this.beliefBase.add(belief)
    }

    // TODO should I have also update belief?
    override suspend fun forget(belief: Belief) {
        this.beliefBase.remove(belief)
    }

    override suspend fun terminate() = stop()

    override suspend fun stop() {
        // TODO not sure this is ok
        // Am I killing the MAS?
        log.d { "Terminating agent" }
        currentCoroutineContext()
            .job.parent
            ?.parent
            ?.cancel()
    }
}
