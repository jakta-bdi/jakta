package it.unibo.jakta.agent.basImpl

import co.touchlab.kermit.Logger
import it.unibo.jakta.agent.AgentLifecycle
import it.unibo.jakta.agent.RunnableAgent
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.baseImpl.GoalFailedEvent
import it.unibo.jakta.intention.IntentionDispatcher
import it.unibo.jakta.plan.Plan
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

class BaseAgentLifecycle<Belief: Any, Goal: Any, Skills: Any>(
    override val runnableAgent: RunnableAgent<Belief, Goal, Skills>
) : AgentLifecycle<Belief, Goal, Skills> {
    private val log =
        Logger(
            Logger.config,
            runnableAgent.id.displayName
        )

    override suspend fun stop() {
        //TODO probably thees needs to be removed from here, as the termination is handled directly by the MAS
        // we could keep it to gracefully shutdown the agent internals (?)
        log.d { "Terminating agent" }
    }

    override suspend fun step(scope: CoroutineScope) {
        log.i { "waiting for event..." }
        val event = runnableAgent.events.next()
        log.i { "received event: $event" }

        when (event) {
            // TODO per rimuovere questo cast dovrei tipare Event.Internal
            //  con Belief e Goal (si può fare ma è subottimo?)
            is AgentEvent.Internal.Belief<*> -> scope.handleBeliefEvent(event as AgentEvent.Internal.Belief<Belief>)
            is AgentEvent.Internal.Goal<*, *> -> scope.handleGoalEvent(event as AgentEvent.Internal.Goal<Goal, Any?>)
            is AgentEvent.Internal.Step -> handleStepEvent(event)
            is AgentEvent.External -> handleExternalEvent(event)
        }
    }

    private fun handleExternalEvent(event: AgentEvent.External) {
        when (event) {
            is AgentEvent.External.Perception -> runnableAgent.state.perceptionHandler(event)
            is AgentEvent.External.Message -> runnableAgent.state.messageHandler(event)
            else -> {
                log.d {
                    "The agent doesn't know what how to handle the event of type ${event::class.qualifiedName}, " +
                        "the default behaviour is skipping it."
                }
                null
            }
            //TODO(Question: is it still possible to handle custom events
            // if the agent internals is implemented in this way?)
        }?.let { runnableAgent.internalInbox.send(it) }
    }

    /**
     * Launches plans in the SupervisorScope of the Step.
     * @param event the belief event that triggered the plan execution.
     */
    private suspend fun CoroutineScope.handleBeliefEvent(event: AgentEvent.Internal.Belief<Belief>) {
        selectPlan(
            entity = event.belief,
            entityMessage = when (event) {
                is AgentEvent.Internal.Belief.Add<Belief> -> "addition of belief"
                is AgentEvent.Internal.Belief.Remove<Belief> -> "removal of belief"
            },
            planList = runnableAgent.state.beliefPlans,
            relevantFilter = {
                when (event) {
                    is AgentEvent.Internal.Belief.Add<Belief> -> it is Plan.Belief.Addition
                    is AgentEvent.Internal.Belief.Remove<Belief> -> it is Plan.Belief.Removal
                } && it.isRelevant(event.belief)
            },
            applicableFilter = {
                it.isApplicable(runnableAgent.state, event.belief)
            },
        )?.let {
            launchPlan(event, event.belief, it)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    // TODO In order to be capable to complete the completion, i had to remove the star projection and put Any?
    //  This requires refactoring of type management
    private suspend fun CoroutineScope.handleGoalEvent(event: AgentEvent.Internal.Goal<Goal, Any?>) {
        selectPlan(
            entity = event.goal,
            entityMessage = when (event) {
                is AgentEvent.Internal.Goal.Add<Goal, *> -> "addition of goal"
                is AgentEvent.Internal.Goal.Remove<Goal, *> -> "removal of goal"
                is AgentEvent.Internal.Goal.Failed<Goal, *> -> "failure of goal"
            },
            planList = runnableAgent.state.goalPlans,
            relevantFilter = {
                when (event) {
                    is AgentEvent.Internal.Goal.Add<Goal, *> -> it is Plan.Goal.Addition
                    is AgentEvent.Internal.Goal.Remove<Goal, *> -> it is Plan.Goal.Removal
                    is AgentEvent.Internal.Goal.Failed<Goal, *> -> it is Plan.Goal.Failure
                } && it.isRelevant(event.goal)
            },
            applicableFilter = {
                it.isApplicable(runnableAgent.state, event.goal)
            },
        )?.let {
            launchPlan(event, event.goal, it, event.completion)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    private suspend fun <TriggerEntity : Any> CoroutineScope.launchPlan(
        event: AgentEvent.Internal,
        entity: TriggerEntity,
        plan: Plan<Belief, Goal, Skills, TriggerEntity, *, *>,
        completion: CompletableDeferred<Any?>? = null, // TODO Check if this Any? can be improved
    ) {
        log.d { "Launching plan $plan for event $event" }
        //val environment: S = currentCoroutineContext()[EnvironmentContext.Key]?.environment as Env
        val intention = runnableAgent.state.mutableIntentionPool.nextIntention(event)

        val interceptor =
            currentCoroutineContext()[ContinuationInterceptor] ?: error { "No ContinuationInterceptor in context" }

        launch(IntentionDispatcher(interceptor) + intention + intention.job) {
            // TODO maybe I should not suppress?? But I want to catch ALL exceptions..
            @Suppress("TooGenericExceptionCaught")
            try {
                log.d { "Running plan $plan" }
                val result = plan.run(runnableAgent.state, entity)
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
        planList: List<Plan<Belief, Goal, *, TriggerEntity, *, *>>,
        relevantFilter: (Plan<Belief, Goal, *, TriggerEntity, *, *>) -> Boolean,
        applicableFilter: (Plan<Belief, Goal, *, TriggerEntity, *, *>) -> Boolean,
    ): Plan<Belief, Goal, Skills, TriggerEntity, *, *>? {
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
            it as Plan<Belief, Goal, Skills, TriggerEntity, *, *>
            //TODO: This is not entirely safe, check from DSL that
            // the type of the agent skill must implement the plan skill type.
        } ?: run {
            log.w { "No plan selected for $entityMessage: $entity" }
            null
        }
    }

    //TODO check if this is enough
    // what happens if a belief plan fails?
    private fun handleFailure(event: AgentEvent.Internal, e: Exception) {
        when (event) {
            is AgentEvent.Internal.Goal.Add<*, *> -> {
                log.d { "Attempting to handle the failure of goal: $event.goal" }
                runnableAgent.internalInbox.send(
                    GoalFailedEvent(
                        event.goal,
                        event.completion,
                        event.intention,
                        event.resultType,
                    ),
                )
            }
            is AgentEvent.Internal.Goal.Failed<*, *> -> {
                log.d { "An error occurred when attempting to handle the failure of goal: $event.goal" }
                event.completion?.completeExceptionally(e)
            }
            else -> {
                log.w { "Handling of event $event failed with exception:${e.message}\n${e.stackTraceToString()}" }
            }
        }
    }

    // TODO(Missing implementation for greedy event selection in case Step.intention was removed from intention pool)
    private suspend fun handleStepEvent(event: AgentEvent.Internal.Step) {
        log.d { "Handling step event for intention ${event.intention.id.displayId}" }
        runnableAgent.state.mutableIntentionPool.stepIntention(event)
    }
}
