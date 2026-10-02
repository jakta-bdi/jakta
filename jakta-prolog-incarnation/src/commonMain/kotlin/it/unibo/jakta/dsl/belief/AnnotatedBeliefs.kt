package it.unibo.jakta.dsl.belief

import it.unibo.jakta.JAKTA_ANNOTATIONS_TAG
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.belief.BeliefRevision
import it.unibo.jakta.belief.BeliefRevision.Revision
import it.unibo.jakta.dsl.agent.AgentBuilder
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.self
import it.unibo.jakta.source
import it.unibo.jakta.tag
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
 * - Replacing the beliefs in a scope shows the scope each source of a belief on its own, e.g. `ping(1)[source(self)]`
 *   and `ping(1)[source(alice)]`, so that a scope can select the contributions of some sources only: see [replaceSelf],
 *   [replaceFrom] and [replaceAll].
 *
 * Use [forgetAllMatching] to forget beliefs whatever their annotations.
 */
object AnnotatedBeliefRevision : BeliefRevision<PrologBelief> {
    // ponytail: linear lookup of the held belief, fine for belief bases of a few hundred beliefs
    private fun Collection<PrologBelief>.held(belief: PrologBelief) = firstOrNull { it == belief }

    override fun add(beliefs: Collection<PrologBelief>, belief: PrologBelief): Revision<PrologBelief> {
        val held = beliefs.held(belief) ?: return Revision(stored = listOf(belief), added = listOf(belief))
        val new = belief.annotations - held.annotations
        return when {
            new.isEmpty() -> Revision()

            else -> Revision(
                stored = listOf(held.withAnnotations(held.annotations + new)),
                added = listOf(belief.withAnnotations(new)),
            )
        }
    }

    override fun remove(beliefs: Collection<PrologBelief>, belief: PrologBelief): Revision<PrologBelief> {
        val held = beliefs.held(belief) ?: return Revision()
        val gone = held.annotations intersect belief.annotations
        val left = held.annotations - gone
        return when {
            gone.isEmpty() -> Revision()

            left.isEmpty() -> Revision(dropped = listOf(held), removed = listOf(held))

            else -> Revision(
                stored = listOf(held.withAnnotations(left)),
                removed = listOf(belief.withAnnotations(gone)),
            )
        }
    }

    override fun replace(
        beliefs: Collection<PrologBelief>,
        scope: (PrologBelief) -> Boolean,
        replacements: List<PrologBelief>,
    ): Revision<PrologBelief> {
        // one replacement per term, with all its annotations
        val incoming = replacements.groupBy { it }.map { (belief, same) ->
            belief.withAnnotations(same.flatMap { it.annotations }.toSet())
        }
        // the contributions in scope that the replacements do not keep, judging each source of a belief on its own
        val outgoing = beliefs.mapNotNull { held ->
            val kept = incoming.held(held)?.annotations.orEmpty()
            val gone = held.annotations.filter { it !in kept && scope(held.withAnnotations(setOf(it))) }.toSet()
            gone.takeIf { it.isNotEmpty() }?.let { held.withAnnotations(it) }
        }
        // removals first, then additions, each applied to the beliefs as revised so far
        var current = beliefs.toList()
        var revision = Revision<PrologBelief>()
        val steps = outgoing.map { belief -> { held: Collection<PrologBelief> -> remove(held, belief) } } +
            incoming.map { belief -> { held: Collection<PrologBelief> -> add(held, belief) } }
        for (step in steps) {
            val next = step(current)
            current = current.revisedBy(next)
            revision += next
        }
        return revision
    }

    private fun List<PrologBelief>.revisedBy(revision: Revision<PrologBelief>): List<PrologBelief> =
        revision.stored.fold(
            filter {
                it !in revision.dropped
            },
        ) { held, stored -> held.filter { it != stored } + stored }
}

/**
 * Revises the agent's beliefs with [AnnotatedBeliefRevision]: beliefs told by several agents, or believed by the
 * agent itself too, are a single belief with all their sources.
 */
fun <Goal : Any, Body : Any> AgentBuilder<PrologBelief, Goal, Body>.usesAnnotatedBeliefs() =
    revisesBeliefsWith(AnnotatedBeliefRevision)

/**
 * The beliefs matching any of [queries] are now [beliefs], whatever their sources: an authoritative observation,
 * which also retracts what other agents told about them.
 */
fun replaceAll(queries: List<Struct>, beliefs: Iterable<PrologBelief>): AgentUpdate.Replace<PrologBelief> =
    AgentUpdate.Replace({ belief -> queries.any { belief.matchBelief(it) != null } }, beliefs.toList())

/**
 * What [from] contributed to the beliefs matching any of [queries] is now [beliefs], attributed to [from]; what other
 * sources contributed stays. The queries must not have annotations.
 */
fun replaceFrom(from: Term, queries: List<Struct>, beliefs: Iterable<PrologBelief>): AgentUpdate.Replace<PrologBelief> {
    require(queries.none { it.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty().isNotEmpty() }) {
        "The queries of a replacement from $from must not have annotations, but got $queries"
    }
    val annotation = source(from)
    val scope = queries.map { it.tag(annotation) }
    return AgentUpdate.Replace(
        { belief -> scope.any { belief.matchBelief(it) != null } },
        beliefs.map { it.withAnnotations(setOf(annotation)) },
    )
}

/**
 * What the agent itself perceived about the beliefs matching any of [queries] is now [beliefs]; what other agents told
 * stays. The usual update for a perception.
 */
fun replaceSelf(queries: List<Struct>, beliefs: Iterable<PrologBelief>): AgentUpdate.Replace<PrologBelief> =
    replaceFrom(self, queries, beliefs)

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
