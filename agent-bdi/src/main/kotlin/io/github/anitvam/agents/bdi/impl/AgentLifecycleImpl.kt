package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentLifecycle
import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.beliefs.BeliefUpdate
import io.github.anitvam.agents.bdi.beliefs.RetrieveResult
import io.github.anitvam.agents.bdi.events.BeliefBaseAddition
import io.github.anitvam.agents.bdi.events.BeliefBaseRemoval
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.goals.Test
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import io.github.anitvam.agents.bdi.goals.BeliefGoal
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.goals.ActionGoal
import io.github.anitvam.agents.bdi.goals.Spawn
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

internal data class AgentLifecycleImpl(val agent: Agent = Agent.empty()) : AgentLifecycle {
    private var context: AgentContext = agent.context

    override fun updateBelief(perceptions: BeliefBase, beliefBase: BeliefBase): RetrieveResult =
        when (perceptions == beliefBase) {
            false -> {
                // 1. each literal l in p not currently in b is added to b
                val rrAddition = beliefBase.addAll(perceptions)

                // 2. each literal l in b no longer in p is deleted from b
                var removedBeliefs = emptyList<BeliefUpdate>()
                var rrRemoval = RetrieveResult(removedBeliefs, rrAddition.updatedBeliefBase)
                rrRemoval.updatedBeliefBase.forEach {
                    if (!perceptions.contains(it)) {
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
            false -> intentions[event.intention!!.id]!!.push(plan.toActivationRecord())
        }

    override fun scheduleIntention(intentions: IntentionPool) = agent.scheduleIntention(intentions)

    override fun runIntention(intention: Intention, context: AgentContext): AgentContext =
        when (val nextGoal = intention.nextGoal()) {
            is ActionGoal -> {
                var newIntention = intention.pop()
                val substitution = context.actionLibrary.invoke(nextGoal) // TODO: Handle failure
                if (newIntention.recordStack.isEmpty()) {
                    context.copy(
                        intentions = IntentionPool.of(context.intentions - newIntention.id)
                    )
                } else {
                    newIntention = newIntention.applySubstitution(substitution)
                    context.copy(
                        intentions = context.intentions.update(newIntention)
                    )
                }
            }
            is Spawn -> context.copy(
                events = context.events + Event.ofAchievementGoalInvocation(Achieve(nextGoal.value)),
            )
            is Achieve -> context.copy(
                events = context.events + Event.ofAchievementGoalInvocation(nextGoal, intention),
                intentions = IntentionPool.of(context.intentions - intention.id),
            )
            is Test -> {
                val solution = context.beliefBase.solve(nextGoal.value)
                var newIntention = intention.pop()
                when (solution.isYes) {
                    true -> newIntention = newIntention.applySubstitution(solution.substitution)
                    else -> TODO("If fails with the bb")
                }
                context.copy(intentions = context.intentions.update(newIntention))
            }
            is BeliefGoal -> when (nextGoal) {
                is AddBelief -> {
                    val retrieveResult = context.beliefBase.add(Belief.of(nextGoal.value))
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs),
                    )
                }
                is RemoveBelief -> {
                    val retrieveResult = context.beliefBase.remove(Belief.of(nextGoal.value))
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs),
                    )
                }
                is UpdateBelief -> {
                    var retrieveResult = context.beliefBase.remove(Belief.of(nextGoal.value))
                    retrieveResult = retrieveResult.updatedBeliefBase.add(Belief.of(nextGoal.value))
                    context.copy(
                        beliefBase = retrieveResult.updatedBeliefBase,
                        events = generateEvents(context.events, retrieveResult.modifiedBeliefs)
                    )
                }
            }
        }

    private fun generateEvents(events: EventQueue, modifiedBeliefs: List<BeliefUpdate>): EventQueue =
        events + modifiedBeliefs.map {
            when (it.updateType) {
                BeliefUpdate.UpdateType.REMOVAL -> Event.of(BeliefBaseRemoval(it.belief))
                BeliefUpdate.UpdateType.ADDITION -> Event.of(BeliefBaseAddition(it.belief))
            }
        }

    override fun reason() {
        // STEP1: Perceive the Environment
        val perceptions = context.perception.percept()

        // STEP2: Update the BeliefBase
        val rr = updateBelief(perceptions, context.beliefBase)
        val newBeliefBase = rr.updatedBeliefBase

        // Generate events related to BeliefBase revision
        var newEvents = generateEvents(context.events, rr.modifiedBeliefs)
        // STEP3: Receiving Communication from Other Agents --> in futuro
        // STEP4: Selecting "Socially Acceptable" Messages --> in futuro

        // STEP5: Selecting an Event.
        val selectedEvent = selectEvent(newEvents)
        var newIntentionPool = context.intentions
        if (selectedEvent != null) {
            newEvents = newEvents - selectedEvent

            // STEP6: Retrieving all Relevant Plans.
            val relevantPlans = selectRelevantPlans(selectedEvent, context.planLibrary)
            // if the set of relevant plans is empty, the event is simply discarded.

            // STEP7: Determining the Applicable Plans.
            val applicablePlans = relevantPlans.plans.filter { isPlanApplicable(selectedEvent, it, context.beliefBase) }

            // STEP8: Selecting one Applicable Plan.
            val selectedPlan = selectApplicablePlan(applicablePlans)

            // STEP9: Select an Intention for Further Execution.
            // Add plan to intentions
            if (selectedPlan != null) {
                val updatedIntention = assignPlanToIntention(selectedEvent, selectedPlan, context.intentions)
                newIntentionPool = context.intentions.update(updatedIntention)
            } // TODO: Gestire un piano rilevante ma NON applicabile
        }

        // Select intention to execute
        var newContext = context.copy(
            events = newEvents,
            beliefBase = newBeliefBase,
            intentions = newIntentionPool,
        )

        if (!newIntentionPool.isEmpty()) {
            val result = scheduleIntention(newIntentionPool)
            val scheduledIntention = result.intentionToExecute
            newIntentionPool = result.newIntentionPool

            // STEP10: Executing one Step on an Intention
            newContext = runIntention(scheduledIntention, newContext.copy(intentions = newIntentionPool))
        }

        this.context = newContext
    }
}
