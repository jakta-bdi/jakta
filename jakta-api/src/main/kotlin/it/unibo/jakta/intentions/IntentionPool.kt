package it.unibo.jakta.intentions

interface IntentionPool<Query : Any, Belief> : Map<IntentionID, Intention<Query, Belief>> {

    fun updateIntention(intention: Intention<Query, Belief>): IntentionPool<Query, Belief>

    fun nextIntention(): Intention<Query, Belief>

    fun pop(): IntentionPool<Query, Belief>

    fun deleteIntention(intentionID: IntentionID): IntentionPool<Query, Belief>
}

// TODO("Fare lo stesso giochetto mutabile <-> immutabile che ho fatto per la bb")
