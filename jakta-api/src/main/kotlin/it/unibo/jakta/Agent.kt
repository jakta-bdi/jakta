package it.unibo.jakta

import it.unibo.jakta.context.MutableAgentContext
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.intentions.SchedulingResult
import it.unibo.jakta.plans.Plan

interface Agent<Query, Belief> {

        val agentID: AgentID

        val name: String

        /** Agent's Actual State */
        val context: MutableAgentContext<*, *, *, *, *>

        /** Event Selection Function*/
        fun selectEvent(events: List<ASEvent>): ASEvent?

        /** Plan Selection Function */
        fun selectApplicablePlan(plans: Iterable<ASPlan>): Plan?

        /** Intention Selection Function */
        fun scheduleIntention(intentions: IntentionPool): SchedulingResult
}