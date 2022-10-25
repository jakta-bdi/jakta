package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.beliefs.BeliefUpdate
import io.github.anitvam.agents.bdi.beliefs.RetrieveResult
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.intentions.SchedulingResult
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

internal class AgentImpl(context: AgentContext) : Agent {
    override var context: AgentContext = context
        private set

    override fun updateBelief(perceptions: BeliefBase, beliefBase: BeliefBase) : RetrieveResult =
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

                RetrieveResult(rrAddition.modifiedBeliefs + rrRemoval.modifiedBeliefs, rrRemoval.updatedBeliefBase)
            }
            else -> RetrieveResult(emptyList(), beliefBase)
        }

    override fun selectEvent(events: EventQueue) = events.first()

    override fun selectRelevantPlans(event: Event, planLibrary: PlanLibrary) = planLibrary.relevantPlans(event)

    override fun isPlanApplicable(event: Event, plan: Plan, beliefBase: BeliefBase) = plan.isApplicable(event, beliefBase)

    override fun selectApplicablePlan(plans: Iterable<Plan>) = plans.first()

    override fun assignPlanToIntention(event: Event, plan: Plan, intentions: IntentionPool) = when (event.isExternal()) {
        true -> Intention.of(plan)
        false -> intentions[event.intention]!!.push(plan.toActivationRecord())
    }

    override fun scheduleIntention(intentions: IntentionPool): SchedulingResult {
        val nextIntention = intentions.nextIntention()
        return SchedulingResult(intentions.pop(), nextIntention)
    }

    override fun runIntention(intention: Intention): Intention {
        when (intention.nextGoal()){
            else -> {}
        }
        return intention.pop()
    }

    override fun reason() {
        // STEP1: Perceive the Environment
        val perceptions = context.perception.percept()

        // STEP2: Update the BeliefBase
        val rr = updateBelief(perceptions, context.beliefBase)
        val newBeliefBase = rr.updatedBeliefBase

        // Generate events related to BeliefBase revision
        var newEvents = context.events + rr.modifiedBeliefs.map {
            when (it.updateType) {
                BeliefUpdate.UpdateType.REMOVAL -> Event.of(Trigger.ofBeliefBaseRemoval(it.belief))
                BeliefUpdate.UpdateType.ADDITION -> Event.of(Trigger.ofBeliefBaseAddition(it.belief))
            }
        }

        // STEP3: Receiving Communication from Other Agents --> in futuro
        // STEP4: Selecting "Socially Acceptable" Messages --> in futuro

        // STEP5: Selecting an Event.
        val selectedEvent = selectEvent(newEvents)
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
        val updatedIntention = assignPlanToIntention(selectedEvent, selectedPlan, context.intentions)

        var newIntentionPool = context.intentions.update(updatedIntention)

        // Select intention to execute
        val result = scheduleIntention(newIntentionPool)
        val scheduledIntention = result.intentionToExecute
        newIntentionPool = result.newIntentionPool

        // STEP10: Executing one Step on an Intention
        val runIntention = runIntention(scheduledIntention)

        context = context.copy(
            beliefBase = newBeliefBase,
            events = newEvents,
            intentions = newIntentionPool.update(runIntention)
        )
    }
}