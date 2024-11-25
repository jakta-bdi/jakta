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
    Event,
    PlanType,
    ActivationRecordType,
    IntentionType,
    ImmutableContext,
> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>,
    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>,
    IntentionType: Intention<Query, Belief, Event, PlanType, ActivationRecordType>,
    ImmutableContext: AgentContext<Query, Belief, Event, PlanType, ActivationRecordType, IntentionType>
{
    val agentID: AgentID

    val name: String

    val lifecycle: AgentLifecycle<
        Query,
        Belief,
        Event,
        PlanType,
        ActivationRecordType,
        IntentionType,
        ImmutableContext
    >

    /** Agent's Actual State */
    val context: MutableAgentContext<
        Query,
        Belief,
        Event,
        PlanType,
        ActivationRecordType,
        IntentionType,
        ImmutableContext
    >

    /** Event Selection Function*/
    fun selectEvent(events: List<Event>): Event?

    /** Plan Selection Function */
    fun selectApplicablePlan(plans: Iterable<PlanType>): PlanType?

    /** Intention Selection Function */
    fun scheduleIntention(
        intentions: IntentionPool<Query, Belief, Event, ActivationRecordType, IntentionType, PlanType>
    ): IntentionType?
}
