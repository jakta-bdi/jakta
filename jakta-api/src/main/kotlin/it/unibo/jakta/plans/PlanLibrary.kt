package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event

//interface PlanLibrary<Query, Belief, BB> : Collection<Plan<Query, Belief, BB>> where
//    Query : Any,
//    BB : BeliefBase<Query, Belief, BB>
//{
//
//    /**
//     * Like a standard practice in prolog, plans are ordered to let programmers know when the end of an eventual recursion happens.
//     */
//    val plans: List<Plan<Query, Belief, BB>>
//
//    /** @return all the relevant [Plan]s from a given [Event] */
//    fun relevantPlans(event: Event): PlanLibrary<Query, Belief, BB, P>
//
//    /** @return all the applicable [Plan]s in the agent with the specified [BeliefBase] */
//    fun applicablePlans(
//        event: Event,
//        beliefBase: BB,
//    ): Collection<Plan<Query, Belief, BB>>
//
//    operator fun plus(plan: Plan<Query, Belief, BB>): PlanLibrary<Query, Belief, BB>
//
//    operator fun minus(plan: Plan<Query, Belief, BB>): PlanLibrary<Query, Belief, BB>
//}
