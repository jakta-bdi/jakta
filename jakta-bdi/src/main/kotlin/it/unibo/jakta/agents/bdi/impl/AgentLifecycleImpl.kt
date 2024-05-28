package it.unibo.jakta.agents.bdi.impl

import it.unibo.jakta.agents.bdi.Agent
import it.unibo.jakta.agents.bdi.AgentLifecycle
import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.InternalAction
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.actions.effects.BeliefChange
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.effects.EventChange
import it.unibo.jakta.agents.bdi.actions.effects.IntentionChange
import it.unibo.jakta.agents.bdi.actions.effects.Pause
import it.unibo.jakta.agents.bdi.actions.effects.PlanChange
import it.unibo.jakta.agents.bdi.actions.effects.PopMessage
import it.unibo.jakta.agents.bdi.actions.effects.Sleep
import it.unibo.jakta.agents.bdi.actions.effects.Stop
import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.jakta.agents.bdi.beliefs.BeliefUpdate
import it.unibo.jakta.agents.bdi.beliefs.RetrieveResult
import it.unibo.jakta.agents.bdi.context.AgentContext
import it.unibo.jakta.agents.bdi.context.ContextUpdate.ADDITION
import it.unibo.jakta.agents.bdi.context.ContextUpdate.REMOVAL
import it.unibo.jakta.agents.bdi.environment.Environment
import it.unibo.jakta.agents.bdi.events.AchievementGoalFailure
import it.unibo.jakta.agents.bdi.events.BeliefBaseAddition
import it.unibo.jakta.agents.bdi.events.BeliefBaseRemoval
import it.unibo.jakta.agents.bdi.events.Event
import it.unibo.jakta.agents.bdi.events.EventQueue
import it.unibo.jakta.agents.bdi.events.TestGoalFailure
import it.unibo.jakta.agents.bdi.executionstrategies.ExecutionResult
import it.unibo.jakta.agents.bdi.goals.Achieve
import it.unibo.jakta.agents.bdi.goals.Act
import it.unibo.jakta.agents.bdi.goals.ActExternally
import it.unibo.jakta.agents.bdi.goals.ActInternally
import it.unibo.jakta.agents.bdi.goals.ActionGoal
import it.unibo.jakta.agents.bdi.goals.AddBelief
import it.unibo.jakta.agents.bdi.goals.BeliefGoal
import it.unibo.jakta.agents.bdi.goals.EmptyGoal
import it.unibo.jakta.agents.bdi.goals.RemoveBelief
import it.unibo.jakta.agents.bdi.goals.Spawn
import it.unibo.jakta.agents.bdi.goals.Test
import it.unibo.jakta.agents.bdi.goals.UpdateBelief
import it.unibo.jakta.agents.bdi.intentions.Intention
import it.unibo.jakta.agents.bdi.intentions.IntentionPool
import it.unibo.jakta.agents.bdi.messages.Tell
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.jakta.agents.bdi.plans.PlanLibrary
import it.unibo.jakta.agents.fsm.Activity

internal data class AgentLifecycleImpl(
    private var agent: Agent,
) : AgentLifecycle {
    private var controller: Activity.Controller? = null
    private var debugEnabled = false
    private var cachedEffects = emptyList<EnvironmentChange>()

    override fun updateBelief(perceptions: BeliefBase, beliefBase: BeliefBase): RetrieveResult =
        when (perceptions == beliefBase) {
            false -> {
                // 1. each literal l in p not currently in b is added to b
                val rrAddition = beliefBase.addAll(perceptions)

                // 2. each literal l in b no longer in p is deleted from b
                var removedBeliefs = emptyList<BeliefUpdate>()
                var rrRemoval = RetrieveResult(removedBeliefs, rrAddition.updatedBeliefBase)
                rrRemoval.updatedBeliefBase.forEach {
                    if (!perceptions.contains(it) && it.rule.head.args.first() == Belief.SOURCE_PERCEPT) {
                        rrRemoval = rrRemoval.updatedBeliefBase.remove(it)
                        removedBeliefs = removedBeliefs + rrRemoval.modifiedBeliefs
                    }
                }

                RetrieveResult(
                    rrAddition.modifiedBeliefs + rrRemoval.modifiedBeliefs,
                    rrRemoval.updatedBeliefBase,
                )
            }
            else -> RetrieveResult(emptyList(), beliefBase)
        }

    override fun selectEvent(events: EventQueue) = agent.selectEvent(events)

    override fun selectRelevantPlans(event: Event, planLibrary: PlanLibrary) = planLibrary.relevantPlans(event)

    override fun isPlanApplicable(event: Event, plan: Plan, beliefBase: BeliefBase) =
        plan.isApplicable(event, beliefBase)

    override fun selectApplicablePlan(plans: Iterable<Plan>) = agent.selectApplicablePlan(plans)

    override fun assignPlanToIntention(event: Event, plan: Plan, intentions: IntentionPool) =
        when (event.isExternal()) {
            true -> Intention.of(plan)
            false -> {
                when (event.trigger) {
                    is AchievementGoalFailure, is TestGoalFailure ->
                        event.intention!!.copy(recordStack = listOf(plan.toActivationRecord()))
                    // else -> intentions[event.intention!!.id]!!.push(plan.toActivationRecord())
                    else -> event.intention!!.pop().push(plan.toActivationRecord())
                }
            }
        }

    override fun scheduleIntention(intentions: IntentionPool) = agent.scheduleIntention(intentions)

    private fun executeInternalAction(
        intention: Intention,
        action: InternalAction,
        context: AgentContext,
        goal: ActionGoal,
    ): ExecutionResult {
        var newIntention = intention.pop()
        if (action.signature.arity < goal.action.args.size) { // Arguments number mismatch
            return ExecutionResult(failAchievementGoal(intention, context))
        } else {
            val internalResponse = action.execute(
                InternalRequest.of(this.agent, controller?.currentTime(), goal.action.args),
            )
            // Apply substitution
            return if (internalResponse.substitution.isSuccess) {
                if (newIntention.recordStack.isNotEmpty()) {
                    newIntention = newIntention.applySubstitution(internalResponse.substitution)
                }
                val newContext = applyEffects(context, internalResponse.effects)
                ExecutionResult(
                    newContext.copy(intentions = newContext.intentions.updateIntention(newIntention)),
                )
            } else {
                ExecutionResult(failAchievementGoal(intention, context))
            }
        }
    }

    private fun executeExternalAction(
        intention: Intention,
        action: ExternalAction,
        context: AgentContext,
        environment: Environment,
        goal: ActionGoal,
    ): ExecutionResult {
        var newIntention = intention.pop()
        if (action.signature.arity < goal.action.args.size) {
            // Argument number mismatch from action definition
            return ExecutionResult(failAchievementGoal(intention, context))
        } else {
            val externalResponse = action.execute(
                ExternalRequest.of(
                    environment,
                    agent.name,
                    controller?.currentTime(),
                    goal.action.args,
                ),
            )
            return if (externalResponse.substitution.isSuccess) {
                if (newIntention.recordStack.isNotEmpty()) {
                    newIntention = newIntention.applySubstitution(externalResponse.substitution)
                }
                ExecutionResult(
                    context.copy(intentions = context.intentions.updateIntention(newIntention)),
                    externalResponse.effects,
                )
            } else {
                ExecutionResult(failAchievementGoal(intention, context))
            }
        }
    }

    override fun runIntention(intention: Intention, context: AgentContext, environment: Environment): ExecutionResult =
        when (val nextGoal = intention.nextGoal()) {
            is EmptyGoal -> ExecutionResult(
                context.copy(intentions = context.intentions.updateIntention(intention.pop())),
            )
            is ActionGoal -> when (nextGoal) {
                is ActInternally -> {
                    val internalAction = context.internalActions[nextGoal.action.functor]

                    if (internalAction == null) {
                        // Internal Action not found
                        if (debugEnabled) {
                            println("[${agent.name}] WARNING: ${nextGoal.action.functor} Internal Action not found.")
                        }
                        ExecutionResult(failAchievementGoal(intention, context))
                    } else {
                        // Execute Internal Action
                        executeInternalAction(intention, internalAction, context, nextGoal)
                    }
                }
                is ActExternally -> {
                    val externalAction = environment.externalActions[nextGoal.action.functor]
                    if (externalAction == null) {
                        // Internal Action not found
                        if (debugEnabled) {
                            println("[${agent.name}] WARNING: ${nextGoal.action.functor} External Action not found.")
                        }
                        ExecutionResult(failAchievementGoal(intention, context))
                    } else {
                        // Execute External Action
                        executeExternalAction(intention, externalAction, context, environment, nextGoal)
                    }
                }
                is Act -> {
                    val action = (environment.externalActions + context.internalActions)[nextGoal.action.functor]
                    if (action == null) {
                        if (debugEnabled) {
                            println("[${agent.name}] WARNING: ${nextGoal.action.functor} Action not found.")
                        }
                        ExecutionResult(failAchievementGoal(intention, context))
                    } else {
                        // Execute Action
                        when (action) {
                            is InternalAction -> executeInternalAction(intention, action, context, nextGoal)
                            is ExternalAction ->
                                executeExternalAction(intention, action, context, environment, nextGoal)
                            else ->
                                throw
                                IllegalStateException("The Action to execute is neither internal nor external")
                        }
                    }
                }
            }
            is Spawn -> ExecutionResult(
                context.copy(
                    events = context.events + Event.ofAchievementGoalInvocation(Achieve.of(nextGoal.value)),
                    intentions = context.intentions.updateIntention(intention.pop()),
                ),
            )
            is Achieve -> ExecutionResult(
                context.copy(
                    events = context.events + Event.ofAchievementGoalInvocation(nextGoal, intention),
                    intentions = IntentionPool.of(context.intentions - intention.id),
                ),
            )
            is Test -> {
                val solution = context.beliefBase.solve(nextGoal.value)
                when (solution.isYes) {
                    true -> ExecutionResult(
                        context.copy(
                            intentions = context.intentions.updateIntention(
                                intention.pop().applySubstitution(solution.substitution),
                            ),
                        ),
                    )
                    else -> ExecutionResult(
                        context.copy(
                            events = context.events + Event.ofTestGoalFailure(intention.currentPlan(), intention),
                        ),
                    )
                }
            }
            is BeliefGoal -> {
                val retrieveResult = when (nextGoal) {
                    is AddBelief -> context.beliefBase.add(Belief.from(nextGoal.value))
                    is RemoveBelief -> context.beliefBase.remove(Belief.from(nextGoal.value))
                    is UpdateBelief -> context.beliefBase.update(Belief.from(nextGoal.value))
                }
                ExecutionResult(
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs),
                        intentions = context.intentions.updateIntention(intention.pop()),
                    ),
                )
            }
        }

    private fun applyEffects(context: AgentContext, effects: Iterable<AgentChange>): AgentContext {
        var newBeliefBase = context.beliefBase
        var newEvents = context.events
        var newPlans = context.planLibrary
        var newIntentions = context.intentions
        effects.forEach {
            when (it) {
                is BeliefChange -> {
                    val rr = when (it.changeType) {
                        ADDITION -> newBeliefBase.add(it.belief)
                        REMOVAL -> newBeliefBase.remove(it.belief)
                    }
                    newBeliefBase = rr.updatedBeliefBase
                    newEvents = generateEvents(newEvents, rr.modifiedBeliefs)
                }
                is IntentionChange -> newIntentions = when (it.changeType) {
                    ADDITION -> newIntentions.updateIntention(it.intention)
                    REMOVAL -> newIntentions.deleteIntention(it.intention.id)
                }
                is EventChange -> newEvents = when (it.changeType) {
                    ADDITION -> newEvents + it.event
                    REMOVAL -> newEvents - it.event
                }
                is PlanChange -> newPlans = when (it.changeType) {
                    ADDITION -> newPlans.addPlan(it.plan)
                    REMOVAL -> newPlans.removePlan(it.plan)
                }

                is Pause -> controller?.pause()
                is Sleep -> controller?.sleep(it.millis)
                is Stop -> controller?.stop()
            }
        }
        return context.copy(
            beliefBase = newBeliefBase,
            events = newEvents,
            planLibrary = newPlans,
            intentions = newIntentions,
        )
    }

    private fun failAchievementGoal(intention: Intention, context: AgentContext) =
        context.copy(
            events = context.events + Event.ofAchievementGoalFailure(intention.currentPlan(), intention),
        )

    private fun generateEvents(events: EventQueue, modifiedBeliefs: List<BeliefUpdate>): EventQueue =
        events + modifiedBeliefs.map {
            when (it.updateType) {
                REMOVAL -> Event.of(BeliefBaseRemoval(it.belief))
                ADDITION -> Event.of(BeliefBaseAddition(it.belief))
            }
        }

    override fun sense(environment: Environment, controller: Activity.Controller?, debugEnabled: Boolean) {
        this.controller = controller
        this.debugEnabled = debugEnabled

        // STEP1: Perceive the Environment
        val perceptions = environment.percept()

        // STEP2: Update the BeliefBase
        val rr = updateBelief(perceptions, agent.context.beliefBase)
        var newBeliefBase = rr.updatedBeliefBase
        // println("pre-run -> $context")
        // Generate events related to BeliefBase revision
        var newEvents = generateEvents(agent.context.events, rr.modifiedBeliefs)

        // STEP3: Receiving Communication from Other Agents
        val message = environment.getNextMessage(agent.name)

        // STEP4: Selecting "Socially Acceptable" Messages //TODO()

        // Parse message
        if (message != null) {
            newEvents = when (message.type) {
                is it.unibo.jakta.agents.bdi.messages.Achieve ->
                    newEvents + Event.ofAchievementGoalInvocation(Achieve.of(message.value))
                is Tell -> {
                    val retrieveResult = newBeliefBase.add(Belief.fromMessageSource(message.from, message.value))
                    newBeliefBase = retrieveResult.updatedBeliefBase
                    generateEvents(newEvents, retrieveResult.modifiedBeliefs)
                }
            }
            cachedEffects = cachedEffects + PopMessage(this.agent.name)
        }
        this.agent = this.agent.copy(newBeliefBase, newEvents)
    }

    override fun deliberate() {
        // STEP5: Selecting an Event.
        var newEvents = this.agent.context.events
        val selectedEvent = selectEvent(this.agent.context.events)
        var newIntentionPool = agent.context.intentions
        if (selectedEvent != null) {
            newEvents = newEvents - selectedEvent

            // STEP6: Retrieving all Relevant Plans.
            val relevantPlans = selectRelevantPlans(selectedEvent, agent.context.planLibrary)
            // if the set of relevant plans is empty, the event is simply discarded.

            // STEP7: Determining the Applicable Plans.
            val applicablePlans = relevantPlans.plans.filter {
                isPlanApplicable(selectedEvent, it, this.agent.context.beliefBase)
            }

            // STEP8: Selecting one Applicable Plan.
            val selectedPlan = selectApplicablePlan(applicablePlans)

            // Add plan to intentions
            if (selectedPlan != null) {
                if (debugEnabled) println("[${agent.name}] Selected the event: $selectedEvent")
                // if (debugEnabled) println("[${agent.name}] Selected the plan: $selectedPlan")
                val updatedIntention = assignPlanToIntention(
                    selectedEvent,
                    selectedPlan.applicablePlan(selectedEvent, this.agent.context.beliefBase),
                    agent.context.intentions,
                )
                // if (debugEnabled) println("[${agent.name}] Updated Intention: $updatedIntention")
                newIntentionPool = agent.context.intentions.updateIntention(updatedIntention)
            } else {
                if (debugEnabled) {
                    println("[${agent.name}] WARNING: There's no applicable plan for the event: $selectedEvent")
                }
                if (selectedEvent.isInternal()) {
                    newIntentionPool = newIntentionPool.deleteIntention(selectedEvent.intention!!.id)
                }
            }
        }
        // Select intention to execute
        this.agent = this.agent.copy(
            events = newEvents,
            intentions = newIntentionPool,
        )
    }

    override fun act(environment: Environment): Iterable<EnvironmentChange> {
        var executionResult = ExecutionResult(AgentContext.of())

        var newIntentionPool = agent.context.intentions
        if (!newIntentionPool.isEmpty()) {
            // STEP9: Select an Intention for Further Execution.
            val result = scheduleIntention(newIntentionPool)
            val scheduledIntention = result.intentionToExecute
            newIntentionPool = result.newIntentionPool
            // STEP10: Executing one Step on an Intention
            this.agent = if (scheduledIntention.recordStack.isEmpty()) {
                this.agent.copy(intentions = newIntentionPool)
            } else {
                // if (debugEnabled) println("[${agent.name}] RUN -> $scheduledIntention")
                executionResult = runIntention(
                    scheduledIntention,
                    this.agent.context.copy(intentions = newIntentionPool),
                    environment,
                )
                this.agent.copy(executionResult.newAgentContext)
            }
            // println("post run -> ${newAgent.context}")
        }

        // Generate Environment Changes
        val environmentChangesToApply = executionResult.environmentEffects + cachedEffects
        cachedEffects = emptyList()
        return environmentChangesToApply
    }

    override fun runOneCycle(
        environment: Environment,
        controller: Activity.Controller?,
        debugEnabled: Boolean,
    ): Iterable<EnvironmentChange> {
        sense(environment, controller, debugEnabled)
        deliberate()
        return act(environment)
    }
}
