package io.github.anitvam.agents.bdi.impl

import io.github.anitvam.agents.bdi.Agent
import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

class AgentImpl(context: AgentContext) : Agent {
    override var context: AgentContext = context
        private set

    override fun updateBelief(perceptions: BeliefBase, beliefBase: BeliefBase): BeliefBase {
        TODO("Not yet implemented")
    }

    override fun selectEvent(events: EventQueue): Event {
        TODO("Not yet implemented")
    }

    override fun selectRelevantPlans(event: Event, planLibrary: PlanLibrary): PlanLibrary {
        TODO("Not yet implemented")
    }

    override fun isPlanApplicable(event: Event, plan: Plan, beliefBase: BeliefBase): Boolean {
        TODO("Not yet implemented")
    }

    override fun selectApplicablePlan(plans: Iterable<Plan>): Plan {
        TODO("Not yet implemented")
    }

    override fun assignPlanToIntention(event: Event, plan: Plan, intentions: IntentionPool): Intention {
        TODO("Not yet implemented")
    }

    override fun scheduleIntention(intentions: IntentionPool): Intention {
        TODO("Not yet implemented")
    }

    override fun runIntention(intention: Intention): Intention {
        TODO("Not yet implemented")
    }

    override fun reason() {
        // STEP1: Perceive the Environment
        val perceptions = context.perception.percept()

        // STEP2: Update the BeliefBase
        val newBeliefBase = updateBelief(perceptions, context.beliefBase)

        // STEP3: Receiving Communication from Other Agents --> in futuro
        // STEP4: Selecting "Socially Acceptable" Messages --> in futuro

        // STEP5: Selecting an Event.
        val selectedEvent = selectEvent(context.events)
        val newEvents = context.events - selectedEvent

        // STEP6: Retrieving all Relevant Plans.
        val relevantPlans = selectRelevantPlans(selectedEvent, context.planLibrary)
        // if the set of relevant plans is empty, the event is simply discarded.

        // STEP7: Determining the Applicable Plans.
        val applicablePlans = relevantPlans.plans.filter { isPlanApplicable(selectedEvent, it, context.beliefBase) }

        // STEP8: Selecting one Applicable Plan.
        val selectedPlan = selectApplicablePlan(applicablePlans)

        // STEP9: Select an Intention for Further Execution.
        val updatedIntention = assignPlanToIntention(selectedEvent, selectedPlan, context.intentions)

        val newIntentionPool = context.intentions.update(updatedIntention)

        val scheduledIntention = scheduleIntention(newIntentionPool)

        // STEP10: Executing one Step on an Intention
        val runIntention = runIntention(scheduledIntention)

        context = context.copy(
            beliefBase = newBeliefBase,
            events = newEvents,
            intentions = context.intentions.update(runIntention)
        )
    }
}