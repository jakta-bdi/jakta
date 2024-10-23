package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event

interface PlanLibrary<PlanTrigger, Guard, Query, Belief, BB, P> where
    Query : Any,
    BB : BeliefBase<Query, Belief, BB>,
    P : Plan<PlanTrigger, Guard, Query, Belief, BB> {
// TODO("ROTTO")
// Così è rotto perchè voglio che il contenuto di planlibrary abbia tanti tipi diversi di plan,
// ognuno con il suo trigger

    /**
     * Like a standard practice in prolog, plans are ordered to let programmers know when the end of an eventual recursion happens.
     */
    val plans: List<P>

    /** @return all the relevant [Plan]s from a given [Event] */
    fun <EventTrigger> relevantPlans(event: Event<EventTrigger>): PlanLibrary<PlanTrigger, Guard, Query, Belief, BB, P>

    /** @return all the applicable [Plan]s in the agent with the specified [BeliefBase] */
    fun <EventTrigger> applicablePlans(
        event: Event<EventTrigger>,
        beliefBase: BB,
    ): PlanLibrary<PlanTrigger, Guard, Query, Belief, BB, P>

    fun addPlan(plan: P):  PlanLibrary<PlanTrigger, Guard, Query, Belief, BB, P>

    fun removePlan(plan: P): PlanLibrary<PlanTrigger, Guard, Query, Belief, BB, P>
}
