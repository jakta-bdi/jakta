package it.unibo.jakta.beliefs

import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<in Query : Any> : Collection<Belief>, EventGenerator<Event.BeliefEvent> {

    /**
     * Performs unification between [B] and values in this [BeliefBase]
     */
    fun select(query: Query): List<Belief> // SelfType
}
