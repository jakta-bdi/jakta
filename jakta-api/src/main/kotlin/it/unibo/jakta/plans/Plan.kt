package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event

interface Plan<Query : Any, Belief> {

    val tasks: List<Task<Query, Belief, *, *>>

    /** Determines if a plan is relevant for the execution of the [Event]  --> UNIFICATION**/
    fun isRelevant(event: Event): Boolean

    /** Determines if a plan is applicable with current knowledge ([BeliefBase])  -->> GUARD*/
    fun isApplicable(event: Event, beliefBase: BeliefBase<Query, Belief>): Boolean
}
