package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event

interface Plan<PlanTrigger, out Guard, Query, Belief, BB> where
    Query: Any,
    BB: BeliefBase<Query, Belief, BB> {

    val trigger: PlanTrigger

    val guard: Guard

    val goals: List<Task>

    /** Determines if a plan is relevant for the execution of the [Event] **/
    fun <EventTrigger> isRelevant(event: Event<EventTrigger>): Boolean

    /** Determines if a plan is applicable with current knowledge ([BeliefBase]) */
    fun <EventTrigger> isApplicable(event: Event<EventTrigger>, beliefBase: BB): Boolean

    /** Returns the computed applicable plan */
    fun <EventTrigger> applicablePlan(
        event: Event<EventTrigger>, beliefBase: BB,
    ): Plan<PlanTrigger, Guard, Query, Belief, BB>

    /**
     * Transforms the current plan into an [ActivationRecord] for the [Intention] that will execute it.
     */
    fun toActivationRecord(): ActivationRecord
}
