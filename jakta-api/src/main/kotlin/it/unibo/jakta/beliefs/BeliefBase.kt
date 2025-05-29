package it.unibo.jakta.beliefs

import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<out Belief : Any> :
    Collection<Belief>,
    EventGenerator<Event.Internal.Belief<Belief>>
