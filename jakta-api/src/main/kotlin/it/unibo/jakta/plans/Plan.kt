package it.unibo.jakta.plans

import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ActivationRecord

// TODO: sealed
interface Plan<Belief : Any, Query : Any, Result> {

    val trigger: Query
    val guard: Query

    val id: PlanID
        get() = PlanID()

    fun apply(event: Event): List<Action<Belief, Query, Result>>

    /** Returns the computed applicable plan */
    fun applicablePlan(event: Event.Internal, beliefBase: BeliefBase<Belief, Query, Result>): Plan<Belief, Query, Result>

    /**
     * Transforms the current plan into an [ActivationRecord] for the [Intention] that will execute it.
     */
    fun toActivationRecord(): ActivationRecord<Belief, Query, Result>

    fun isApplicable(event: Event.Internal, beliefBase: BeliefBase<Belief, Query, Result>): Boolean

    fun isRelevant(event: Event.Internal): Boolean

    interface AddBelief<Belief : Any, Query : Any, Result> : Plan<Belief, Query, Result>
    interface RemoveBelief<Belief : Any, Query : Any, Result> : Plan<Belief, Query, Result>
    interface AddGoal<Belief : Any, Query : Any, Result> : Plan<Belief, Query, Result>
    interface RemoveGoal<Belief : Any, Query : Any, Result> : Plan<Belief, Query, Result>
    interface Test<Belief : Any, Query : Any, Result> : Plan<Belief, Query, Result>
    interface FailTest<Belief : Any, Query : Any, Result> : Plan<Belief, Query, Result>
}
