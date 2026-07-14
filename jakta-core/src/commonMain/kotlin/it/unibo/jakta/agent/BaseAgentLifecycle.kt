package it.unibo.jakta.agent

import co.touchlab.kermit.Logger
import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.event.GoalAddEvent
import it.unibo.jakta.event.GoalFailedEvent
import it.unibo.jakta.event.GoalRemoveEvent
import it.unibo.jakta.intention.IntentionDispatcher
import it.unibo.jakta.plan.Plan
import kotlin.coroutines.ContinuationInterceptor
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
        when (event) {
            // TODO to remove this cast i should type the top level Event.Internal
            //  with Belief and Goal. Doable but maybe unnecessary?

            is AgentEvent.Internal.Belief<*> -> scope.handleBeliefEvent(event as AgentEvent.Internal.Belief<Belief>)

            is AgentEvent.Internal.Goal<*, *> -> scope.handleGoalEvent(event as AgentEvent.Internal.Goal<Goal, Any?>)

            is AgentEvent.Internal.Step -> handleStepEvent(event)

            is AgentEvent.External -> handleExternalEvent(event)
        }
    }

    /**
     * Launches plans in the SupervisorScope of the Step.
     * @param event the belief event that triggered the plan execution.
     */
    private fun CoroutineScope.handleBeliefEvent(event: AgentEvent.Internal.Belief<Belief>) {
        selectPlan(
            entity = event.belief,
            entityMessage = when (event) {
                is AgentEvent.Internal.Belief.Add<Belief> -> "addition of belief"
                is AgentEvent.Internal.Belief.Remove<Belief> -> "removal of belief"
            },
            planList = executableAgent.state.beliefPlans,
            relevantFilter = {
                when (event) {
                    is AgentEvent.Internal.Belief.Add<Belief> -> it is Plan.Belief.Addition<Belief, Goal, *, *>
                    is AgentEvent.Internal.Belief.Remove<Belief> -> it is Plan.Belief.Removal<Belief, Goal, *, *>
                } && it.isRelevant(event.belief)
            },
            applicableFilter = {
                it.isApplicable(executableAgent.state.beliefs, event.belief)
            },
        )?.let {
            launchPlan(event, event.belief, it)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    // TODO In order to be capable to complete the completion, i had to remove the star projection and put Any?
    //  This requires refactoring of type management
    private fun CoroutineScope.handleGoalEvent(event: AgentEvent.Internal.Goal<Goal, Any?>) {
        selectPlan(
            entity = event.goal,
            entityMessage = when (event) {
                is AgentEvent.Internal.Goal.Add<Goal, *> -> "addition of goal"
                is AgentEvent.Internal.Goal.Remove<Goal, *> -> "removal of goal"
                is AgentEvent.Internal.Goal.Failed<Goal, *> -> "failure of goal"
            },
            planList = executableAgent.state.goalPlans,
            relevantFilter = {
                when (event) {
                    is AgentEvent.Internal.Goal.Add<Goal, *> -> it is Plan.Goal.Addition<Belief, Goal, *, *>
                    is AgentEvent.Internal.Goal.Remove<Goal, *> -> it is Plan.Goal.Removal<Belief, Goal, *, *>
                    is AgentEvent.Internal.Goal.Failed<Goal, *> -> it is Plan.Goal.Failure<Belief, Goal, *, *>
                } && it.isRelevant(event.goal)
            },
            applicableFilter = {
                it.isApplicable(executableAgent.state.beliefs, event.goal)
            },
        )?.let {
            launchPlan(event, event.goal, it, event.completion)
        } ?: run {
            handleFailure(event, Exception("No plan found for $event"))
        }
    }

    private fun <TriggerEntity : Any> CoroutineScope.launchPlan(
        event: AgentEvent.Internal,
        entity: TriggerEntity,
        plan: Plan<Belief, Goal, TriggerEntity, *, *>,
        completion: CompletableDeferred<Any?>? = null, // TODO Check if this Any? can be improved
    ) {
        val intention = executableAgent.state.mutableIntentionPool.nextIntention(
            event,
            this.coroutineContext.job,
        )
        log.d { "Launching plan $plan for event $event on intention $intention" }
        val interceptor =
            this.coroutineContext[ContinuationInterceptor] ?: error { "No ContinuationInterceptor in context" }

        launch(IntentionDispatcher(interceptor) + intention + intention.job) {
            @Suppress("TooGenericExceptionCaught")
            try {
                log.d { "Running plan $plan for event $event" }
                val result = plan.run(executableAgent.state, entity)
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

    private fun <TriggerEntity : Any> selectPlan(
        entity: TriggerEntity,
        entityMessage: String,
        planList: List<Plan<Belief, Goal, TriggerEntity, *, *>>,
        relevantFilter: (Plan<Belief, Goal, TriggerEntity, *, *>) -> Boolean,
        applicableFilter: (Plan<Belief, Goal, TriggerEntity, *, *>) -> Boolean,
    ): Plan<Belief, Goal, TriggerEntity, *, *>? {
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
