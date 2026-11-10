package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.event.GoalFailedEvent
import it.unibo.jakta.event.GoalRemoveEvent
import it.unibo.jakta.intention.IntentionDispatcher
import it.unibo.jakta.plan.Option
import it.unibo.jakta.plan.Plan
import kotlin.coroutines.ContinuationInterceptor
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * A base implementation of the [it.unibo.jakta.agent.AgentLifecycle] interface
 * that defines the core logic for handling agent events and executing plans.
 */
class BaseAgentLifecycle<Belief : Any, Goal : Any>(override val executableAgent: ExecutableAgent<Belief, Goal>) :
    AgentLifecycle<Belief, Goal> {
    private val log =
        Logger(
            Logger.config,
            executableAgent.id.displayName,
        )

    // TODO consider making this public or add a method to cancel it e.g. stop()
    //  so far it does not seem to be necessary
    private val agentJob = SupervisorJob()

    override suspend fun step() {
        log.i { "waiting for event..." }
        val event = executableAgent.events.next()
        log.i { "received event: $event" }
        val scope = CoroutineScope(currentCoroutineContext() + agentJob)
        handleEvent(event, scope)
    }

    override fun tryStep(dispatcher: CoroutineDispatcher) {
        executableAgent.events.tryNext()?.let {
            val scope = CoroutineScope(dispatcher + agentJob)
            handleEvent(it, scope)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleEvent(event: AgentEvent, scope: CoroutineScope) {
        completeWaitingEventFilters(event)
        when (event) {
            is AgentEvent.Internal -> {
                // TODO to remove this cast i should type the top level Event.Internal
                //  with Belief and Goal. Doable but maybe unnecessary?
                when (event) {
                    is AgentEvent.Internal.Belief<*> ->
                        scope.handleBeliefEvent(event as AgentEvent.Internal.Belief<Belief>)

                    is AgentEvent.Internal.Goal<*, *> ->
                        scope.handleGoalEvent(event as AgentEvent.Internal.Goal<Goal, Any?>)

                    is AgentEvent.Internal.Step ->
                        handleStepEvent(event)
                }
            }

            is AgentEvent.External -> handleExternalEvent(event)
        }
    }

    /**
     * Iterates over the map of waiting event filters.
     * If the filter matches complete the associated deferred and remove the filter from the map.
     */
    @Suppress("UNCHECKED_CAST") // TODO Check
    private fun completeWaitingEventFilters(event: AgentEvent) {
        for ((filter, deferred) in executableAgent.state.waitEventFilters) {
            val result = filter(event)
            if (result != null) {
                (deferred as CompletableDeferred<Any>).complete(result)
            }
        }
    }

    /**
     * Launches plans in the SupervisorScope of the Step.
     * @param event the belief event that triggered the plan execution.
     */
    private fun CoroutineScope.handleBeliefEvent(event: AgentEvent.Internal.Belief<Belief>) {
        selectPlan(
            event = event,
            entity = event.belief,
            entityMessage = when (event) {
                is AgentEvent.Internal.Belief.Add<Belief> -> "addition of belief"
                is AgentEvent.Internal.Belief.Remove<Belief> -> "removal of belief"
            },
            planList = executableAgent.state.beliefPlans.filter {
                when (event) {
                    is AgentEvent.Internal.Belief.Add<Belief> -> it is Plan.Belief.Addition<Belief, Goal, *, *>
                    is AgentEvent.Internal.Belief.Remove<Belief> -> it is Plan.Belief.Removal<Belief, Goal, *, *>
                }
            },
            desiredResult = typeOf<Any?>(),
        )?.let {
            launchPlan(event, it)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    // TODO In order to be capable to complete the completion, i had to remove the star projection and put Any?
    //  This requires refactoring of type management
    private fun CoroutineScope.handleGoalEvent(event: AgentEvent.Internal.Goal<Goal, Any?>) {
        selectPlan(
            event = event,
            entity = event.goal,
            entityMessage = when (event) {
                is AgentEvent.Internal.Goal.Add<Goal, *> -> "addition of goal"
                is AgentEvent.Internal.Goal.Remove<Goal, *> -> "removal of goal"
                is AgentEvent.Internal.Goal.Failed<Goal, *> -> "failure of goal"
            },
            planList = executableAgent.state.goalPlans.filter {
                when (event) {
                    is AgentEvent.Internal.Goal.Add<Goal, *> -> it is Plan.Goal.Addition<Belief, Goal, *, *>
                    is AgentEvent.Internal.Goal.Remove<Goal, *> -> it is Plan.Goal.Removal<Belief, Goal, *, *>
                    is AgentEvent.Internal.Goal.Failed<Goal, *> -> it is Plan.Goal.Failure<Belief, Goal, *, *>
                }
            },
            desiredResult = event.resultType,
        )?.let {
            launchPlan(event, it, event.completion)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    /**
     * Launches the selected [option] on an intention. The plan runs with the context of the option,
     * computed at selection time: its trigger and guard are not evaluated again when it starts.
     */
    private fun CoroutineScope.launchPlan(
        event: AgentEvent.Internal,
        option: Option<Belief, Goal, *, *, *>,
        completion: CompletableDeferred<Any?>? = null, // TODO Check if this Any? can be improved
    ) {
        val plan = option.plan
        val intention = executableAgent.state.mutableIntentionPool.nextIntention(
            event,
            this.coroutineContext.job,
        )
        log.d { "Launching plan $plan for event $event on intention $intention" }
        val interceptor =
            this.coroutineContext[ContinuationInterceptor] ?: error("No ContinuationInterceptor in context")

        launch(IntentionDispatcher(interceptor) + intention + intention.job) {
            @Suppress("TooGenericExceptionCaught")
            try {
                log.d { "Running plan $plan for event $event" }
                val result = option.run(executableAgent.state)
                completion?.complete(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.e("Goal failed for exception: ${e.message} ${e.stackTraceToString()}")
                handleFailure(event, e)
            }
        }.invokeOnCompletion {
            log.d { "Plan Completed: $plan for event $event" }
        }
    }

    /**
     * Selects the plan of [planList] to run for [event] with the agent's
     * [it.unibo.jakta.plan.PlanSelection], as an [Option] carrying the context computed by its trigger and guard.
     * Each trigger is evaluated once and each guard at most once, all on the same snapshot of the beliefs:
     * the options are computed lazily, so a guard is only evaluated if the selection asks for that option.
     */
    private fun <TriggerEntity : Any> selectPlan(
        event: AgentEvent.Internal,
        entity: TriggerEntity,
        entityMessage: String,
        planList: List<Plan<Belief, Goal, TriggerEntity, *, *>>,
        desiredResult: KType,
    ): Option<Belief, Goal, *, *, *>? {
        val relevant = planList.mapNotNull { it.relevantOption(entity, desiredResult) }

        if (relevant.isEmpty()) {
            log.w { "No relevant plans for $entityMessage: $entity" }
            return null
        }

        val beliefs = executableAgent.state.beliefs
        val options = relevant.asSequence().mapNotNull { it(beliefs) }.constrainOnce()
        return executableAgent.planSelection.select(event, options)?.also {
            log.d { "Selected plan ${it.plan} for $entityMessage: $entity" }
        } ?: run {
            log.w { "No plan selected for $entityMessage: $entity" }
            null
        }
    }

    /**
     * Evaluates the trigger of this plan for [entity].
     * @return null if the plan is not relevant, otherwise a function evaluating the guard on some beliefs.
     */
    private fun <TriggerEntity : Any, Context : Any, PlanResult> Plan<Belief, Goal, TriggerEntity, Context, PlanResult>
        .relevantOption(
            entity: TriggerEntity,
            desiredResult: KType,
        ): ((Collection<Belief>) -> Option<Belief, Goal, TriggerEntity, Context, PlanResult>?)? =
        relevantContext(entity, desiredResult)?.let { triggerContext ->
            { beliefs -> optionFor(beliefs, triggerContext) }
        }

    private fun handleFailure(event: AgentEvent.Internal, e: Exception) {
        when (event) {
            is AgentEvent.Internal.Goal.Add<*, *> -> {
                log.d { "Attempting to handle the failure of goal: $event.goal" }
                executableAgent.internalInbox.send(
                    GoalFailedEvent(
                        event.goal,
                        event.resultType,
                        event.completion,
                        event.intention,
                    ),
                )
            }

            is AgentEvent.Internal.Goal.Failed<*, *> -> {
                log.d { "An error occurred when attempting to handle the failure of goal: $event.goal" }
                event.completion?.completeExceptionally(e)
            }

            else -> {
                log.w { "Handling of event $event failed with exception:${e.message}" }
            }
        }
    }

    private fun handleStepEvent(event: AgentEvent.Internal.Step) {
        log.d { "Handling step event for intention ${event.intention.id.displayId}" }
        executableAgent.state.mutableIntentionPool.stepIntention(event)
    }

    // TODO unsafe cast, should be avoided, but I don't think it is possible
    private fun handleExternalEvent(event: AgentEvent.External) {
        val update = when (event) {
            is AgentEvent.External.Perception -> executableAgent.state.run {
                perceptionHandler(event)
            }

            is AgentEvent.External.Message<*> -> executableAgent.state.run {
                messageHandler(event)
            }
        } ?: run {
            log.i { "The external event $event has been received but not mapped into an update" }
            return
        }

        @Suppress("UNCHECKED_CAST")
        when (update) {
            is AgentUpdate.Belief<*> ->
                handleBeliefUpdateEvent(update as AgentUpdate.Belief<Belief>)

            is AgentUpdate.Goal<*> ->
                handleGoalUpdateEvent(update as AgentUpdate.Goal<Goal>)
        }
    }

    private fun handleBeliefUpdateEvent(event: AgentUpdate.Belief<Belief>) {
        log.i { "Handling belief update event $event" }
        val additionsOnly = event.additions - event.removals
        val removalsOnly = event.removals - event.additions
        removalsOnly.forEach { executableAgent.state.forget(it) }
        additionsOnly.forEach { executableAgent.state.believe(it) }
    }

    // TODO this should change to have a proper set of desires
    //  for now I simply forward the goal addition and removal events
    // TODO this has problems because goal removal is not actually stopping goals already in pursue.
    private fun handleGoalUpdateEvent(event: AgentUpdate.Goal<Goal>) {
        log.i { "Handling goal update event $event" }
        val additionsOnly = event.additions - event.removals
        val removalsOnly = event.removals - event.additions
        removalsOnly.forEach {
            executableAgent.internalInbox.send(
                GoalRemoveEvent.withNoResult(it),
            )
        }
        additionsOnly.forEach {
            executableAgent.internalInbox.send(
                GoalAddEvent.withNoResult(it),
            )
        }
    }
}
