package it.unibo.jakta

import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.context.MutableAgentContext
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan

interface Agent<Query: Any, Belief> {

        val agentID: AgentID

        val name: String

        /** Agent's Actual State */
        val context: MutableAgentContext<Query, Belief, AgentContext<Query, Belief>>

        /** Event Selection Function*/
        fun selectEvent(events: List<Event>): Event?

        /** Plan Selection Function */
        fun selectApplicablePlan(plans: Iterable<Plan<Query, Belief>>): Plan<Query, Belief>?

        /** Intention Selection Function */
        fun scheduleIntention(intentions: IntentionPool<Query, Belief>): Intention<Query, Belief>?
}