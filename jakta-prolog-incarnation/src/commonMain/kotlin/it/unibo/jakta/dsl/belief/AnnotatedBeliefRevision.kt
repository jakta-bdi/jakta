package it.unibo.jakta.dsl.belief

import it.unibo.jakta.belief.BeliefRevision
import it.unibo.jakta.belief.BeliefRevision.Revision
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.effectiveAnnotations
import it.unibo.jakta.setAnnotations

/**
 * Belief revision with Jason's annotation semantics: a belief is identified by its term, and its annotations
 * (e.g. its sources) are a set that grows and shrinks.
 *
 * - A belief without annotations is the agent's own, and is stored as such: believing `ping(1)` stores
 *   `ping(1)[source(self)]`. Other sources, such as `source(percept)`, are up to whoever produces the belief, e.g. the
 *   perception handler.
 * - Adding a belief already held, with new annotations, stores a single belief with all of them, and announces the
 *   addition of the belief with only the new ones: telling `ping(1)` to an agent that was told it by Alice makes it
 *   believe `ping(1)[source(alice), source(carol)]`, and raises `+ping(1)[source(carol)]`.
 * - Removing a belief removes only its annotations, and the belief when none is left: forgetting `ping(1)`, i.e.
 *   `ping(1)[source(self)]`, leaves `ping(1)[source(alice)]` if Alice told it too, and raises `-ping(1)[source(self)]`.
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

    override fun add(current: Collection<PrologBelief>, toAdd: PrologBelief): Revision<PrologBelief> {
        val held = current.held(toAdd)
            ?: return super.add(current, toAdd.setAnnotations(*toAdd.effectiveAnnotations.toTypedArray()))
        val newAnnotations = toAdd.effectiveAnnotations - held.effectiveAnnotations
        return when {
            newAnnotations.isEmpty() -> Revision()

            else -> Revision(
                add = listOf(held.setAnnotations(*(held.effectiveAnnotations + newAnnotations).toTypedArray())),
                announceAdded = listOf(toAdd.setAnnotations(*newAnnotations.toTypedArray())),
            )
        }
    }

    override fun remove(current: Collection<PrologBelief>, toRemove: PrologBelief): Revision<PrologBelief> {
        val held = current.held(toRemove) ?: return Revision()
        val removedAnnotations = held.effectiveAnnotations intersect toRemove.effectiveAnnotations
        val keptAnnotations = held.effectiveAnnotations - removedAnnotations
        return when {
            removedAnnotations.isEmpty() -> Revision()

            keptAnnotations.isEmpty() -> Revision(remove = listOf(held), announceRemoved = listOf(held))

            else -> Revision(
                add = listOf(held.setAnnotations(*keptAnnotations.toTypedArray())),
                announceRemoved = listOf(toRemove.setAnnotations(*removedAnnotations.toTypedArray())),
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
            belief.setAnnotations(*same.flatMap { it.effectiveAnnotations }.toTypedArray())
        }
        // the annotations the scope removes from each belief held, except those the replacements keep
        val outdated = current.mapNotNull { held ->
            val inScope = scope(held) ?: return@mapNotNull null
            val wantedAnnotations = wanted.held(held)?.effectiveAnnotations.orEmpty()
            val inScopeAnnotations = held.effectiveAnnotations intersect inScope.effectiveAnnotations
            val outdatedAnnotations = inScopeAnnotations - wantedAnnotations
            outdatedAnnotations.takeIf { it.isNotEmpty() }?.let { held.setAnnotations(*it.toTypedArray()) }
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
