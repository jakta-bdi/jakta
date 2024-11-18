package it.unibo.jakta

import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.context.MutableAgentContext
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Plan

interface Agent<
    Query,
    Belief,
    EventType,
    PlanType,
    ActivationRecordType,
    IntentionType,
    out ImmutableContext,
> where
    Query: Any,
    EventType: Event,
    PlanType: Plan<Query, Belief>,
    ActivationRecordType: ActivationRecord<Query, Belief>,
    IntentionType: Intention<Query, Belief, ActivationRecordType>,
    ImmutableContext: AgentContext<Query, Belief, EventType, PlanType>
{

    val agentID: AgentID

    val name: String

    val lifecycle: AgentLifecycle<Query, Belief>

    /** Agent's Actual State */
    val context: MutableAgentContext<
        Query,
        Belief,
        EventType,
        PlanType,
        ActivationRecordType,
        IntentionType,
        ImmutableContext
    >

    /** Event Selection Function*/
    fun selectEvent(events: List<EventType>): Event?

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<PlanType>): PlanType?

    /** Intention Selection Function */
    fun scheduleIntention(intentions: IntentionPool<Query, Belief, ActivationRecordType, IntentionType>): IntentionType?
}
