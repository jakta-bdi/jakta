package it.unibo.jakta.beliefs

import it.unibo.jakta.events.Event
import it.unibo.jakta.events.EventGenerator

/** A BDI Agent's collection of [Belief]s */
interface BeliefBase<out Belief : Any> :
    Set<Belief>,
    EventGenerator<Event.Internal.Belief<Belief>> {
}

operator fun <Belief : Any>  BeliefBase<Belief>.plus(other: BeliefBase<Belief>): BeliefBase<Belief> =
    object : BeliefBase<Belief> {
        override val size: Int
            get() = this@plus.size + other.size

        override fun isEmpty(): Boolean = this@plus.isEmpty() || other.isEmpty()

        override fun contains(element: Belief): Boolean = element in this@plus || element in other

        override fun iterator(): Iterator<Belief> = sequenceOf(this@plus, other).flatten().iterator()

        override fun containsAll(elements: Collection<Belief>) = (elements - this@plus - other).isEmpty()

        override fun poll(): Event.Internal.Belief<Belief>? = this@plus.poll() ?: other.poll()
    }
