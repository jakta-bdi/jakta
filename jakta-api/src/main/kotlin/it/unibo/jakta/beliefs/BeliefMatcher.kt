package it.unibo.jakta.beliefs

interface BeliefMatcher<Belief : Any, Query : Any , Result> {

    fun select(query: Query, base : BeliefBase<Belief, Query, Result>): Result

}