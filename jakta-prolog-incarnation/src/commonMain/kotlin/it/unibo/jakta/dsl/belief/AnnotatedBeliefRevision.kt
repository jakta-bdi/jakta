package it.unibo.jakta.dsl.belief

import it.unibo.jakta.annotations
import it.unibo.jakta.belief.BeliefRevision
import it.unibo.jakta.belief.BeliefRevision.Revision
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.isSource
import it.unibo.jakta.self
import it.unibo.jakta.setAnnotations
import it.unibo.jakta.source
import it.unibo.tuprolog.core.Struct

/**
 * Belief revision with Jason's annotation semantics: a belief is identified by its term, and its annotations
 * (e.g. its sources) are a set that grows and shrinks.
 *
 * - A belief without a source is the agent's own, and is stored as such: believing `ping(1)` stores
 *   `ping(1)[source(self)]`, and `ping(1)[certain]` stores `ping(1)[certain, source(self)]`. Other sources, such as
 *   `source(percept)`, are up to whoever produces the belief, e.g. the perception handler.
 * - Adding a belief already held, with new annotations, stores a single belief with all of them, and announces the
 *   addition of the belief with only the new ones: telling `ping(1)` to an agent that was told it by Alice makes it
 *   believe `ping(1)[source(alice), source(carol)]`, and raises `+ping(1)[source(carol)]`.
 * - Removing a belief removes only its annotations, and the belief when no source is left, whatever its other
 *   annotations: forgetting `ping(1)`, i.e. `ping(1)[source(self)]`, leaves `ping(1)[source(alice)]` if Alice told it
 *   too, and raises `-ping(1)[source(self)]`; if not, it removes `ping(1)` altogether.
 *
 * - Replacing removes from each belief held the annotations its scope gives, unless the replacements keep them: a scope
 *   giving `ping(1)[source(self)]` for `ping(1)[source(self), source(alice)]` replaces only the agent's own
 *   contribution, while one giving the whole belief replaces every source. See [replaceFrom].
 *
 * Use [forgetAllMatching] to forget beliefs whatever their annotations.
 */
object AnnotatedBeliefRevision : BeliefRevision<PrologBelief> {
    // ponytail: linear lookup of the held belief, fine for belief bases of a few hundred beliefs
    private fun Collection<PrologBelief>.held(belief: PrologBelief) = firstOrNull { it == belief }

    // what the revision is given without a source is the agent's own; what it holds always has a source
    private fun Set<Struct>.withImplicitSelf(): Set<Struct> = if (none { it.isSource }) this + source(self) else this

    override fun add(current: Collection<PrologBelief>, toAdd: PrologBelief): Revision<PrologBelief> {
        val held = current.held(toAdd)
            ?: return super.add(current, toAdd.setAnnotations(toAdd.annotations.withImplicitSelf()))
        val newAnnotations = toAdd.annotations.withImplicitSelf() - held.annotations
        return when {
            newAnnotations.isEmpty() -> Revision()

            else -> Revision(
                add = listOf(held.setAnnotations(held.annotations + newAnnotations)),
                announceAdded = listOf(toAdd.setAnnotations(newAnnotations)),
            )
        }
    }

    override fun remove(current: Collection<PrologBelief>, toRemove: PrologBelief): Revision<PrologBelief> {
        val held = current.held(toRemove) ?: return Revision()
        val removedAnnotations = held.annotations intersect toRemove.annotations.withImplicitSelf()
        val keptAnnotations = held.annotations - removedAnnotations
        return when {
            removedAnnotations.isEmpty() -> Revision()

            // a belief lives as long as it has a source
            keptAnnotations.none { it.isSource } -> Revision(remove = listOf(held), announceRemoved = listOf(held))

            else -> Revision(
                add = listOf(held.setAnnotations(keptAnnotations)),
                announceRemoved = listOf(toRemove.setAnnotations(removedAnnotations)),
            )
        }
    }

    override fun replace(
        current: Collection<PrologBelief>,
        replacements: List<PrologBelief>,
        scope: (PrologBelief) -> PrologBelief?,
    ): Revision<PrologBelief> {
        // one replacement per term, with all its annotations
        val wanted = replacements.groupBy { it }.map { (belief, same) ->
            belief.setAnnotations(same.flatMap { it.annotations.withImplicitSelf() }.toSet())
        }
        // the annotations the scope removes from each belief held, except those the replacements keep
        val outdated = current.mapNotNull { held ->
            val inScope = scope(held) ?: return@mapNotNull null
            val wantedAnnotations = wanted.held(held)?.annotations.orEmpty()
            val inScopeAnnotations = held.annotations intersect inScope.annotations.withImplicitSelf()
            val outdatedAnnotations = inScopeAnnotations - wantedAnnotations
            outdatedAnnotations.takeIf { it.isNotEmpty() }?.let { held.setAnnotations(it) }
        }
        // removals first, then additions, each applied to the beliefs as revised so far
        var revised = current.toList()
        var revision = Revision<PrologBelief>()
        val steps = outdated.map { belief -> { held: Collection<PrologBelief> -> remove(held, belief) } } +
            wanted.map { belief -> { held: Collection<PrologBelief> -> add(held, belief) } }
        for (step in steps) {
            val next = step(revised)
            revised = revised.revisedBy(next)
            revision += next
        }
        return revision
    }

    private fun List<PrologBelief>.revisedBy(revision: Revision<PrologBelief>): List<PrologBelief> =
        revision.add.fold(filter { it !in revision.remove }) { held, stored -> held.filter { it != stored } + stored }
}

/**
 * Revises the agent's beliefs with [AnnotatedBeliefRevision]: beliefs told by several agents, or believed by the
 * agent itself too, are a single belief with all their sources.
 */
fun <Goal : Any, Body : Any> AgentBuilder<PrologBelief, Goal, Body>.usesAnnotatedBeliefs() =
    revisesBeliefsWith(AnnotatedBeliefRevision)
