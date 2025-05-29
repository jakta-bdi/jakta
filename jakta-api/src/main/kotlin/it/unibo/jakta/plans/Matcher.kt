package it.unibo.jakta.plans

import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.MutableBeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ActivationRecord

interface Matcher<Belief : Any, Query : Any, Response> {

    fun query(query: Query, base: BeliefBase<Belief>): Response?

    fun matchPlanFor(
        event: Event.Internal,
        plans: Collection<Plan<Belief, Query, Response>>,
        beliefBase: BeliefBase<Belief>,
    ): ActivationRecord<Belief, Query, Response>?
}

interface Modifier<Belief : Any, Modify : Any, Response> {
    fun modifyBeliefBase(beliefBase: MutableBeliefBase<Belief>, modify: Modify): Response
}
