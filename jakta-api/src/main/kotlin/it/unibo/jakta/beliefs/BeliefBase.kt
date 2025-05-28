package it.unibo.jakta.beliefs

import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<out Belief : Any, in Query : Any, out Result> : Collection<Belief>, EventGenerator<Event.Internal.Belief<Belief>> {

    /**
     * Performs unification between [B] and values in this [BeliefBase]
     */
    fun select(query: Query): Result
}
