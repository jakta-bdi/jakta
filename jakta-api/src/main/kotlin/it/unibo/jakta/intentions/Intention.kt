package it.unibo.jakta.intentions


interface Intention<Belief : Any, Query : Any, Result> {
    val stack: List<ActivationRecord<Belief, Query, Result>>
    val id : IntentionID
}