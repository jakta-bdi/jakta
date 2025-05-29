package it.unibo.jakta.plans

import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ActivationRecord

interface PlanMatcher<Belief : Any, Query : Any, Result> {

    fun select(event: Event.Internal, plans : Sequence<Plan<Belief, Query, Result>>) : ActivationRecord<Belief, Query, Result>?

}