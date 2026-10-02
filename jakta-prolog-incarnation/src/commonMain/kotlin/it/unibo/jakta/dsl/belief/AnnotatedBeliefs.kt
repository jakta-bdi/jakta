package it.unibo.jakta.dsl.belief

import it.unibo.jakta.JAKTA_ANNOTATIONS_TAG
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.belief.BeliefRevision
import it.unibo.jakta.belief.BeliefRevision.Revision
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.self
import it.unibo.jakta.source
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.utils.setTag

/**
 * The annotations of a belief; a belief without annotations counts as `[source(self)]`.
 */
val PrologBelief.annotations: Set<Struct>
    get() = head.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG)?.takeIf { it.isNotEmpty() } ?: setOf(source(self))

/**
 * The same belief, with exactly [annotations].
 */
fun PrologBelief.withAnnotations(annotations: Set<Struct>): PrologBelief {
    val annotated = head.setTag(JAKTA_ANNOTATIONS_TAG, annotations)
    return if (isFact) Fact.of(annotated) else Rule.of(annotated, body)
}

/**
 * Belief revision with Jason's annotation semantics: a belief is identified by its term, and its annotations
 * (e.g. its sources) are a set that grows and shrinks.
 *
 * - Adding a belief already held, with new annotations, stores a single belief with all of them, and announces the
 *   addition of the belief with only the new ones: telling `ping(1)` to an agent that was told it by Alice makes it
 *   believe `ping(1)[source(alice), source(carol)]`, and raises `+ping(1)[source(carol)]`.
 * - Removing a belief removes only its annotations, and the belief when none is left: forgetting `ping(1)`, i.e.
 *   `ping(1)[source(self)]`, leaves `ping(1)[source(alice)]` if Alice told it too, and raises `-ping(1)[source(self)]`.
 *
 * Use [forgetAllMatching] to forget beliefs whatever their annotations.
 */
object AnnotatedBeliefRevision : BeliefRevision<PrologBelief> {
    // ponytail: linear lookup of the held belief, fine for belief bases of a few hundred beliefs
    private fun Collection<PrologBelief>.held(belief: PrologBelief) = firstOrNull { it == belief }

    override fun add(beliefs: Collection<PrologBelief>, belief: PrologBelief): Revision<PrologBelief> {
        val held = beliefs.held(belief) ?: return Revision(stored = setOf(belief), added = setOf(belief))
        val new = belief.annotations - held.annotations
        return when {
            new.isEmpty() -> Revision()

            else -> Revision(
                stored = setOf(held.withAnnotations(held.annotations + new)),
                added = setOf(belief.withAnnotations(new)),
            )
        }
    }

    override fun remove(beliefs: Collection<PrologBelief>, belief: PrologBelief): Revision<PrologBelief> {
        val held = beliefs.held(belief) ?: return Revision()
        val gone = held.annotations intersect belief.annotations
        val left = held.annotations - gone
        return when {
            gone.isEmpty() -> Revision()
            left.isEmpty() -> Revision(dropped = setOf(held), removed = setOf(held))
            else -> Revision(stored = setOf(held.withAnnotations(left)), removed = setOf(belief.withAnnotations(gone)))
        }
    }
}

/**
 * Revises the agent's beliefs with [AnnotatedBeliefRevision]: beliefs told by several agents, or believed by the
 * agent itself too, are a single belief with all their sources.
 */
fun <Goal : Any, Body : Any> AgentBuilder<PrologBelief, Goal, Body>.usesAnnotatedBeliefs() =
    revisesBeliefsWith(AnnotatedBeliefRevision)

/**
 * Forgets every belief matching [beliefQuery], whatever its annotations.
 */
fun MutableAgentState<PrologBelief, *>.forgetAllMatching(beliefQuery: Struct) =
    beliefs.filter { it.matchBelief(beliefQuery) != null }.forEach { forget(it) }

/**
 * The sources of the beliefs matching [beliefQuery]: the agents that told them, and `self` for the agent's own.
 * Unlike a `[source(S)]` pattern, which binds `S` to a single source, it collects them all.
 */
fun Collection<PrologBelief>.sourcesOf(beliefQuery: Struct): Set<Term> = filter { it.matchBelief(beliefQuery) != null }
    .flatMap { it.annotations }
    .filter { it.functor == "source" && it.arity == 1 }
    .map { it[0] }
    .toSet()
