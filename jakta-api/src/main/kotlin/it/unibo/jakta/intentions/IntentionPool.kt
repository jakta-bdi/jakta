package it.unibo.jakta.intentions

import it.unibo.jakta.plans.Plan

interface IntentionPool<
    Query,
    Belief,
    Event,
    ActivationRecordType,
    IntentionType,
    PlanType,
> : Map<IntentionID, IntentionType> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>,
    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>,
    IntentionType: Intention<Query, Belief, Event, PlanType, ActivationRecordType>
{
    fun nextIntention(): IntentionType
}

interface MutableIntentionPool<
    Query,
    Belief,
    Event,
    ActivationRecordType,
    IntentionType,
    PlanType,
> : Map<IntentionID, IntentionType>,
    IntentionPool<Query, Belief, Event, ActivationRecordType, IntentionType, PlanType> where
    Query: Any,
    PlanType: Plan<Query, Belief, Event>,
    ActivationRecordType: ActivationRecord<Query, Belief, Event, PlanType>,
    IntentionType: Intention<Query, Belief, Event, PlanType, ActivationRecordType>
{
    fun updateIntention(intention: IntentionType): Boolean

    fun pop(): IntentionType?

    fun deleteIntention(intentionID: IntentionID): IntentionType?

    fun snapshot(): IntentionPool<Query, Belief, Event, ActivationRecordType, IntentionType, PlanType>
}
