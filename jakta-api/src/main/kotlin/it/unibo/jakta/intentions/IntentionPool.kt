package it.unibo.jakta.intentions

interface IntentionPool<Query, Belief, ActivationRecordType, IntentionType> : Map<IntentionID, IntentionType> where
    Query: Any,
    ActivationRecordType: ActivationRecord<Query, Belief>,
    IntentionType: Intention<Query, Belief, ActivationRecordType>
{

    fun updateIntention(intention: IntentionType): IntentionPool<Query, Belief, ActivationRecordType, IntentionType>

    fun nextIntention(): IntentionType

    fun pop(): IntentionPool<Query, Belief, ActivationRecordType, IntentionType>

    fun deleteIntention(intentionID: IntentionID): IntentionPool<Query, Belief, ActivationRecordType, IntentionType>
}
