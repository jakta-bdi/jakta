package it.unibo.jakta.belief

import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
import it.unibo.jakta.event.Event
import it.unibo.jakta.event.EventInbox

internal data class BeliefBaseImpl<Belief : Any>(
    private val events: EventInbox<Event.Internal.Belief<Belief>>,
    val initialBeliefs: Iterable<Belief> = emptyList(),
    private val beliefs: MutableSet<Belief> = mutableSetOf()
) : BeliefBase<Belief>,
    MutableSet<Belief> by beliefs {

    /**
     * Triggers events for the addition of the initial beliefs.
      */
    init {
        initialBeliefs.forEach { add(it) }
    }

    override fun snapshot(): Collection<Belief> = this.copy()

    override fun add(element: Belief): Boolean = beliefs.add(element).alsoWhenTrue {
        events.send(BeliefAddEvent(element))
    }

    override fun remove(element: Belief): Boolean = beliefs.remove(element).alsoWhenTrue {
        events.send(BeliefRemoveEvent(element))
    }

    override fun addAll(elements: Collection<Belief>): Boolean = elements.map { add(it) }.any { it }

    override fun removeAll(elements: Collection<Belief>): Boolean = elements.map { remove(it) }.any { it }

    override fun retainAll(elements: Collection<Belief>): Boolean = beliefs.filter { it !in elements }
        .map { remove(it) }
        .any { it }

    override fun clear() = beliefs.map { BeliefRemoveEvent(it) }
        .forEach { events.send(it) }
        .run { beliefs.clear() }

    companion object {
        private fun Boolean.alsoWhenTrue(action: () -> Unit): Boolean {
            if (this) action()
            return this
        }
    }
}
