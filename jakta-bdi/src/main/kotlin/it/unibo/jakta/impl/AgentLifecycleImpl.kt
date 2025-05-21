package it.unibo.jakta.impl

import it.unibo.jakta.ASAgent
import it.unibo.jakta.ASAgentLifecycle
import it.unibo.jakta.AgentProcess
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.effects.PlanChange
import it.unibo.jakta.actions.effects.PopMessage
import it.unibo.jakta.actions.effects.Sleep
import it.unibo.jakta.actions.effects.Stop
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.ASMutableBeliefBase
import it.unibo.jakta.environment.BasicEnvironment
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.events.BeliefBaseAddition
import it.unibo.jakta.events.BeliefBaseRemoval
import it.unibo.jakta.events.TestGoalFailure
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.jakta.fsm.Activity
import it.unibo.jakta.intentions.ASIntention
import it.unibo.jakta.intentions.ASIntentionPool
import it.unibo.jakta.messages.Tell
import it.unibo.jakta.plans.ASPlan

//class LifeCycle {
//    val agent: Agent<String> = TODO()
//    var todos: List<Action>
//
//    suspend fun runCycle() {
//        val nextEvent = agent.sense()
//        if (nextEvent != null) {
//            todos += agent.deliberate(nextEvent)
//        }
//        val sideEffects = todos.first()()
//        sideEffects.forEach { it() }
//    }
//}

internal data class AgentLifecycleImpl(
    private var agent: ASAgent,
) : ASAgentLifecycle {
    private var cachedEffects = emptyList<EnvironmentChange>()

    override fun selectEvent(events: List<ASEvent>): ASEvent? = agent.selectEvent(events)

    override fun updateBelief(
        perceptions: ASBeliefBase,
        beliefBase: ASMutableBeliefBase,
    ): Boolean = when (perceptions == beliefBase) {
        false -> {
            // 1. each literal l in p not currently in b is added to b
            beliefBase.addAll(perceptions)

            // 2. each literal l in b no longer in p is deleted from b
            beliefBase.forEach {
                if (!perceptions.contains(it) && it.content.head.args.first() == ASBelief.SOURCE_PERCEPT) {
                    beliefBase.remove(it)
                }
            }
            true
        }
        else -> false
    }

    override fun selectRelevantPlans(
        event: ASEvent,
        planLibrary: List<ASPlan>
    ): List<ASPlan> = planLibrary.filter { it.isRelevant(event) }

    override fun isPlanApplicable(
        event: ASEvent,
        plan: ASPlan,
        beliefBase: ASBeliefBase,
    ): Boolean = plan.isApplicable(event, beliefBase)

    override fun selectApplicablePlan(plans: List<ASPlan>) = agent.selectApplicablePlan(plans)

    override fun assignPlanToIntention(
        event: ASEvent,
        plan: ASPlan,
        intentions: ASIntentionPool,
    ): ASIntention? = when (event.isExternal()) {
        true -> ASIntention.of(plan)
        false -> {
            when (event) {
                is AchievementGoalFailure, is TestGoalFailure ->
                    event.intention?.copy(recordStack = mutableListOf(plan.toActivationRecord()))
                // else -> intentions[event.intention!!.id]!!.push(plan.toActivationRecord())
                else -> {
                    event.intention?.pop()
                    event.intention?.push(plan.toActivationRecord())
                    event.intention
                }
            }
        }
    }

    override fun scheduleIntention(intentions: ASIntentionPool) = agent.scheduleIntention(intentions)
    override fun runIntention(
        intention: ASIntention,
        agent: ASAgent,
        environment: AgentProcess
    ) {
        TODO("Not yet implemented")
    }

    override fun sense(environment: AgentProcess) {
        TODO("Not yet implemented")
    }

    override fun runIntention(intention: ASIntention, agent: ASAgent, environment: BasicEnvironment): ExecutionResult {
        when (val nextGoal = intention.nextTask()) {

            is ActionGoal -> when (nextGoal) {
                is ActInternally -> {
                    val internalAction = context.internalActions[nextGoal.action.functor]

                    if (internalAction == null) {
                        // Internal ASAction not found
                        if (debugEnabled) {
                            println("[${agent.name}] WARNING: ${nextGoal.action.functor} Internal ASAction not found.")
                        }
                        ExecutionResult(failAchievementGoal(intention, context))
                    } else {
                        // Execute Internal ASAction
                        executeInternalAction(intention, internalAction, context, nextGoal)
                    }
                }

                is ActExternally -> {
                    val externalAction = environment.externalActions[nextGoal.action.functor]
                    if (externalAction == null) {
                        // Internal ASAction not found
                        if (debugEnabled) {
                            println("[${agent.name}] WARNING: ${nextGoal.action.functor} External ASAction not found.")
                        }
                        ExecutionResult(failAchievementGoal(intention, context))
                    } else {
                        // Execute External ASAction
                        executeExternalAction(intention, externalAction, context, environment, nextGoal)
                    }
                }

                is Act -> {
                    val action = (environment.externalActions + context.internalActions)[nextGoal.action.functor]
                    if (action == null) {
                        if (debugEnabled) {
                            println("[${agent.name}] WARNING: ${nextGoal.action.functor} ASAction not found.")
                        }
                        ExecutionResult(failAchievementGoal(intention, context))
                    } else {
                        // Execute ASAction
                        when (action) {
                            is InternalAction -> executeInternalAction(intention, action, context, nextGoal)
                            is ExternalAction ->
                                executeExternalAction(intention, action, context, environment, nextGoal)

                            else ->
                                throw IllegalStateException("The ASAction to execute is neither internal nor external")
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

            else -> {}
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

    override fun sense(environment: BasicEnvironment, controller: Activity.Controller?, debugEnabled: Boolean) {
        this.controller = controller
        this.debugEnabled = debugEnabled

        // STEP1: Perceive the BasicEnvironment
        val perceptions = environment.percept()

        // STEP2: Update the ASBeliefBase
        val rr = updateBelief(perceptions, agent.context.beliefBase)
        var newBeliefBase = rr.updatedBeliefBase
        // println("pre-run -> $context")
        // Generate events related to ASBeliefBase revision
        var newEvents = generateEvents(agent.context.events, rr.modifiedBeliefs)

        // STEP3: Receiving Communication from Other Agents
        val message = environment.getNextMessage(agent.name)

        // STEP4: Selecting "Socially Acceptable" Messages //TODO()

        // Parse message
        if (message != null) {
            newEvents = when (message.type) {
                is it.unibo.jakta.messages.Achieve ->
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
        val roContext = this.agent.context.snapshot()
        val selectedEvent = selectEvent(roContext.events)
        if (selectedEvent != null) {
            agent.context.mutableEventList.remove(selectedEvent)

            // STEP6: Retrieving all Relevant Plans.
            val relevantPlans = selectRelevantPlans(selectedEvent, roContext.planLibrary.toList())
            // if the set of relevant plans is empty, the event is simply discarded.

            // STEP7: Determining the Applicable Plans.
            val applicablePlans = relevantPlans.filter {
                isPlanApplicable(selectedEvent, it, roContext.beliefBase)
            }

            // STEP8: Selecting one Applicable Plan.
            val selectedPlan = selectApplicablePlan(applicablePlans)

            // Add plan to intentions
            if (selectedPlan != null) {
                if (this.agent.environment.debugEnabled)
                    println("[${agent.name}] Selected the event: $selectedEvent")
                // if (debugEnabled) println("[${agent.name}] Selected the plan: $selectedPlan")
                val updatedIntention = assignPlanToIntention(
                    selectedEvent,
                    selectedPlan.applicablePlan(selectedEvent, roContext.beliefBase as ASBeliefBase), //Todo(remove this cast)
                    roContext.intentions,
                )
                // if (debugEnabled) println("[${agent.name}] Updated Intention: $updatedIntention")
                if (updatedIntention != null) {
                    agent.context.mutableIntentionPool.updateIntention(updatedIntention)
                }
            } else {
                if (agent.environment.debugEnabled) {
                    println("[${agent.name}] WARNING: There's no applicable plan for the event: $selectedEvent")
                }
                if (selectedEvent.isInternal()) {
                    agent.context.mutableIntentionPool.deleteIntention(selectedEvent.intention!!.id)
                }
            }
        }
    }

    override fun act(environment: AgentProcess): Boolean {
        TODO("Not yet implemented")
    }

    override fun act(environment: BasicEnvironment): Boolean {
        var executionResult = ExecutionResult(AgentContext.of())
        // Select intention to execute
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

        // Generate BasicEnvironment Changes
        val environmentChangesToApply = executionResult.environmentEffects + cachedEffects
        cachedEffects = emptyList()
        return environmentChangesToApply
    }

    override fun runOneCycle(
        environment: BasicEnvironment,
        debugEnabled: Boolean,
    ): Boolean {
        sense(environment, debugEnabled)
        deliberate()
        return act(environment)
    }
}
