package it.unibo.jakta.belief

import it.unibo.jakta.event.AgentEvent
import it.unibo.jakta.event.BeliefAddEvent
import it.unibo.jakta.event.BeliefRemoveEvent
import it.unibo.jakta.event.EventInbox

internal data class BeliefBaseImpl<Belief : Any>(
    private val events: EventInbox<AgentEvent.Internal.Belief<Belief>>,
    val initialBeliefs: Iterable<Belief> = emptyList(),
    private val revisionStrategy: BeliefRevision<Belief> = BeliefRevision.plain(),
    private val beliefs: MutableSet<Belief> = mutableSetOf(),
) : BeliefBase<Belief>,
    MutableSet<Belief> by beliefs {

    init {
        // the initial beliefs are revised as any other, but raise no events
        initialBeliefs.forEach { apply(revisionStrategy.add(beliefs, it), notify = false) }
    }

    override fun snapshot(): Collection<Belief> = beliefs.toSet()

    override fun add(element: Belief): Boolean = apply(revisionStrategy.add(beliefs, element))

    override fun remove(element: Belief): Boolean = apply(revisionStrategy.remove(beliefs, element))

    override fun replace(replacements: List<Belief>, scope: (Belief) -> Belief?): Boolean {
        val outOfScope = replacements.filter { scope(it) == null }
        require(outOfScope.isEmpty()) { "Replacements must be in the scope they replace, but $outOfScope are not" }
        return apply(revisionStrategy.replace(beliefs, replacements, scope))
    }

    private fun apply(revision: BeliefRevision.Revision<Belief>, notify: Boolean = true): Boolean {
        revision.remove.forEach { beliefs.remove(it) }
        revision.add.forEach {
            // a set keeps the element it already has: replace it, so that an equal but revised belief is stored
            beliefs.remove(it)
            beliefs.add(it)
        }
        if (notify) {
            revision.announceRemoved.forEach { events.send(BeliefRemoveEvent(it)) }
            revision.announceAdded.forEach { events.send(BeliefAddEvent(it)) }
        }
        return revision.add.isNotEmpty() || revision.remove.isNotEmpty()
    }

    override fun addAll(elements: Collection<Belief>): Boolean = elements.map { add(it) }.any { it }

    override fun removeAll(elements: Collection<Belief>): Boolean = elements.map { remove(it) }.any { it }

    override fun retainAll(elements: Collection<Belief>): Boolean = beliefs.filter { it !in elements }
        .map { remove(it) }
        .any { it }

    override fun clear() = beliefs.map { BeliefRemoveEvent(it) }
        .forEach { events.send(it) }
        .run { beliefs.clear() }
}
