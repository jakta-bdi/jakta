package it.unibo.jakta.plans

import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.BeliefBase

interface Plan<Query : Any, Belief, Event> {

    val tasks: List<Action<*, *, *>>

    /** Determines if a plan is relevant for the execution of the [Event]  --> UNIFICATION**/
    fun isRelevant(event: Event): Boolean

    /** Determines if a plan is applicable with current knowledge ([BeliefBase])  -->> GUARD*/
    fun isApplicable(event: Event, beliefBase: BeliefBase<Query, Belief>): Boolean
}
