package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.ContextUpdate.ADDITION
import io.github.anitvam.agents.bdi.ContextUpdate.REMOVAL
import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.beliefs.BeliefUpdate
import io.github.anitvam.agents.bdi.beliefs.RetrieveResult
import io.github.anitvam.agents.bdi.events.AchievementGoalFailure
import io.github.anitvam.agents.bdi.events.BeliefBaseAddition
import io.github.anitvam.agents.bdi.events.BeliefBaseRemoval
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.TestGoalFailure
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.ActionGoal
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.goals.BeliefGoal
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import io.github.anitvam.agents.bdi.goals.Spawn
import io.github.anitvam.agents.bdi.goals.Test
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.effects.BeliefChange
import io.github.anitvam.agents.bdi.actions.effects.EventChange
import io.github.anitvam.agents.bdi.actions.effects.IntentionChange
import io.github.anitvam.agents.bdi.actions.effects.PlanChange
import io.github.anitvam.agents.bdi.environment.Environment
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

internal data class AgentLifecycleImpl(
    private var agent: Agent,
) : AgentLifecycle {
    // private var context: AgentContext = agent.context

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

    override fun runIntention(intention: Intention, context: AgentContext): AgentContext =
        when (val nextGoal = intention.nextGoal()) {
            is ActionGoal -> when (nextGoal) {
                is ActInternally -> {
                    var newIntention = intention.pop()
                    val internalAction = context.internalActions[nextGoal.action.functor]

                    if (internalAction == null) {
                        // Internal Action not found
                        failAchievementGoal(intention, context)
                    } else {
                        // Execute Internal Action
                        val internalResponse = internalAction.execute(
                            InternalRequest.of(this.agent, nextGoal.action.args)
                        )
                        // Apply substitution
                        if (internalResponse.substitution.isSuccess) {
                            if (newIntention.recordStack.isNotEmpty()) {
                                newIntention = newIntention.applySubstitution(internalResponse.substitution)
                            }
                            val newContext = applyEffects(context, internalResponse.effects)
                            newContext.copy(intentions = newContext.intentions.updateIntention(newIntention))
                        } else {
                            failAchievementGoal(intention, context)
                        }
                    }
                }
                is Act -> TODO()
            }
            is Spawn -> context.copy(
                events = context.events + Event.ofAchievementGoalInvocation(Achieve.of(nextGoal.value)),
            )
            is Achieve -> context.copy(
                events = context.events + Event.ofAchievementGoalInvocation(nextGoal, intention),
                intentions = IntentionPool.of(context.intentions - intention.id),
            )
            is Test -> {
                val solution = context.beliefBase.solve(nextGoal.value)
                when (solution.isYes) {
                    true -> context.copy(
                        intentions = context.intentions.updateIntention(
                            intention.pop().applySubstitution(solution.substitution)
                        )
                    )
                    else -> context.copy(
                        events = context.events + Event.ofTestGoalFailure(intention.currentPlan(), intention)
                    )
                }
            }
            is BeliefGoal -> when (nextGoal) {
                is AddBelief -> {
                    val retrieveResult = context.beliefBase.add(Belief.fromSelfSource(nextGoal.value))
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs),
                    )
                }
                is RemoveBelief -> {
                    val retrieveResult = context.beliefBase.remove(Belief.fromSelfSource(nextGoal.value))
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs),
                    )
                }
                is UpdateBelief -> {
                    var retrieveResult = context.beliefBase.remove(Belief.fromSelfSource(nextGoal.value))
                    retrieveResult = retrieveResult.updatedBeliefBase.add(Belief.fromSelfSource(nextGoal.value))
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs)
                    )
                }
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

    override fun reason(environment: Environment) {
        // STEP1: Perceive the Environment
        val perceptions = agent.context.perception.percept()

        // STEP2: Update the BeliefBase
        val rr = updateBelief(perceptions, agent.context.beliefBase)
        val newBeliefBase = rr.updatedBeliefBase
        // println("pre-run -> $context")
        // Generate events related to BeliefBase revision
        var newEvents = generateEvents(agent.context.events, rr.modifiedBeliefs)
        // STEP3: Receiving Communication from Other Agents --> in futuro
        // STEP4: Selecting "Socially Acceptable" Messages --> in futuro

        // STEP5: Selecting an Event.
        val selectedEvent = selectEvent(newEvents)
        var newIntentionPool = agent.context.intentions
        if (selectedEvent != null) {
            newEvents = newEvents - selectedEvent

            // STEP6: Retrieving all Relevant Plans.
            val relevantPlans = selectRelevantPlans(selectedEvent, agent.context.planLibrary)
            // if the set of relevant plans is empty, the event is simply discarded.

            // STEP7: Determining the Applicable Plans.
            val applicablePlans = relevantPlans.plans.filter { isPlanApplicable(selectedEvent, it, newBeliefBase) }

            // STEP8: Selecting one Applicable Plan.
            val selectedPlan = selectApplicablePlan(applicablePlans)

            // STEP9: Select an Intention for Further Execution.
            // Add plan to intentions
            if (selectedPlan != null) {
                // println("EVENT: $selectedEvent")
                val updatedIntention = assignPlanToIntention(
                    selectedEvent,
                    selectedPlan.applicablePlan(selectedEvent, newBeliefBase),
                    agent.context.intentions,
                )
                newIntentionPool = agent.context.intentions.updateIntention(updatedIntention)
            } else {
                println("WARNING: There's no applicable plan for the event: $selectedEvent")
                if (selectedEvent.isInternal()) {
                    newIntentionPool = newIntentionPool.deleteIntention(selectedEvent.intention!!.id)
                }
            }
        }
        // Select intention to execute
        var newAgent = agent.copy(
            events = newEvents,
            beliefBase = newBeliefBase,
            intentions = newIntentionPool,
        )
        // println("PREPARED CONTEXT -> $newContext")
        // println(newIntentionPool)
        if (!newIntentionPool.isEmpty()) {
            val result = scheduleIntention(newIntentionPool)
            val scheduledIntention = result.intentionToExecute
            newIntentionPool = result.newIntentionPool
            // STEP10: Executing one Step on an Intention
            newAgent = if (scheduledIntention.recordStack.isEmpty()) {
                newAgent.copy(intentions = newIntentionPool)
            } else {
                // println("RUN -> $scheduledIntention")
                newAgent.copy(runIntention(scheduledIntention, newAgent.context.copy(intentions = newIntentionPool)))
            }
        }
        // println("post run -> $newContext")
        this.agent = newAgent
    }
}
