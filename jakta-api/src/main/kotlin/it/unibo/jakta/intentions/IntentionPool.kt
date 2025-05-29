package it.unibo.jakta.intentions

interface IntentionPool<Belief : Any, Query : Any, Result> : Map<IntentionID, Intention<Belief, Query, Result>> {
    fun nextIntention(): Intention<Belief, Query, Result>
}

interface MutableIntentionPool<Belief : Any, Query : Any, Result> : IntentionPool<Belief, Query, Result> {
    fun updateIntention(intention: Intention<Belief, Query, Result>): Boolean
    fun pop(): Intention<Belief, Query, Result>?
    fun deleteIntention(intentionID: IntentionID): Intention<Belief, Query, Result>?
    fun snapshot(): IntentionPool<Belief, Query, Result>
}
