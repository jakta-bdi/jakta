package it.unibo.jakta.intentions

interface IntentionPool<
    Query,
    Belief,
    Event,
    ActivationRecordType,
    IntentionType
> : Map<IntentionID, IntentionType> where
    Query: Any,
    ActivationRecordType: ActivationRecord<Query, Belief, Event>,
    IntentionType: Intention<Query, Belief, Event, ActivationRecordType>
{

    fun updateIntention(intention: IntentionType): IntentionPool<
        Query,
        Belief,
        Event,
        ActivationRecordType,
        IntentionType
    >

    fun nextIntention(): IntentionType

    fun pop(): IntentionPool<
        Query,
        Belief,
        Event,
        ActivationRecordType,
        IntentionType
    >

    fun deleteIntention(intentionID: IntentionID): IntentionPool<
        Query,
        Belief,
        Event,
        ActivationRecordType,
        IntentionType
    >
}
