package io.github.anitvam.agents.bdi

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.EventQueue
import io.github.anitvam.agents.bdi.intentions.Intention
import io.github.anitvam.agents.bdi.intentions.IntentionPool
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary

interface Agent {
    val context: AgentContext
//    val reasoningCycle: ReasoningCycle

    fun updateBelief(perceptions: BeliefBase, beliefBase: BeliefBase): BeliefBase
    fun selectEvent(events: EventQueue): Event
    fun selectRelevantPlans(event: Event, planLibrary: PlanLibrary): PlanLibrary
    fun isPlanApplicable(event: Event, plan: Plan, beliefBase: BeliefBase): Boolean
    fun selectApplicablePlan(plans: Iterable<Plan>): Plan
    fun assignPlanToIntention(event: Event, plan: Plan, intentions: IntentionPool): Intention
    fun scheduleIntention(intentions: IntentionPool): Intention
    fun runIntention(intention: Intention): Intention

    fun reason()
}

