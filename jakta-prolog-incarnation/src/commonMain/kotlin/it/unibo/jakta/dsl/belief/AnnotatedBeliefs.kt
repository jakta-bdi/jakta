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
import it.unibo.tuprolog.core.Var
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
 * - Replacing removes from each belief held the annotations its scope gives, unless the replacements keep them: a scope
 *   giving `ping(1)[source(self)]` for `ping(1)[source(self), source(alice)]` replaces only the agent's own
 *   contribution, while one giving the whole belief replaces every source. See [replaceSelf], [replaceFrom] and
 *   [replaceAll].
 *
 * Use [forgetAllMatching] to forget beliefs whatever their annotations.
 */
object AnnotatedBeliefRevision : BeliefRevision<PrologBelief> {
    // ponytail: linear lookup of the held belief, fine for belief bases of a few hundred beliefs
    private fun Collection<PrologBelief>.held(belief: PrologBelief) = firstOrNull { it == belief }

    override fun add(current: Collection<PrologBelief>, toAdd: PrologBelief): Revision<PrologBelief> {
        val held = current.held(toAdd) ?: return super.add(current, toAdd)
        val newAnnotations = toAdd.annotations - held.annotations
        return when {
            newAnnotations.isEmpty() -> Revision()

            else -> Revision(
                add = listOf(held.withAnnotations(held.annotations + newAnnotations)),
                announceAdded = listOf(toAdd.withAnnotations(newAnnotations)),
            )
        }
    }

    override fun remove(current: Collection<PrologBelief>, toRemove: PrologBelief): Revision<PrologBelief> {
        val held = current.held(toRemove) ?: return Revision()
        val removedAnnotations = held.annotations intersect toRemove.annotations
        val keptAnnotations = held.annotations - removedAnnotations
        return when {
            removedAnnotations.isEmpty() -> Revision()

            keptAnnotations.isEmpty() -> Revision(remove = listOf(held), announceRemoved = listOf(held))

            else -> Revision(
                add = listOf(held.withAnnotations(keptAnnotations)),
                announceRemoved = listOf(toRemove.withAnnotations(removedAnnotations)),
            )
        }
    }

    override fun replace(
        current: Collection<PrologBelief>,
        scope: (PrologBelief) -> PrologBelief?,
        replacements: List<PrologBelief>,
    ): Revision<PrologBelief> {
        // one replacement per term, with all its annotations
        val wanted = replacements.groupBy { it }.map { (belief, same) ->
            belief.withAnnotations(same.flatMap { it.annotations }.toSet())
        }
        // the annotations the scope removes from each belief held, except those the replacements keep
        val outdated = current.mapNotNull { held ->
            val inScope = scope(held) ?: return@mapNotNull null
            val wantedAnnotations = wanted.held(held)?.annotations.orEmpty()
            val outdatedAnnotations = (held.annotations intersect inScope.annotations) - wantedAnnotations
            outdatedAnnotations.takeIf { it.isNotEmpty() }?.let { held.withAnnotations(it) }
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

/**
 * The beliefs matching any of [queries] are now [beliefs], whatever their sources: an authoritative observation,
 * which also retracts what other agents told about them.
 */
fun replaceAll(queries: List<Struct>, beliefs: Iterable<PrologBelief>): AgentUpdate.Replace<PrologBelief> =
    AgentUpdate.Replace.where(beliefs.toList()) { belief -> queries.any { belief.matchBelief(it) != null } }

/**
 * What [from] contributed to the beliefs matching any of [queries] is now [beliefs], attributed to [from]; what other
 * sources contributed stays. The queries must not have annotations.
 */
fun replaceFrom(from: Term, queries: List<Struct>, beliefs: Iterable<PrologBelief>): AgentUpdate.Replace<PrologBelief> =
    replacementFrom(from, queries, beliefs)

/**
 * What the agent itself perceived about the beliefs matching any of [queries] is now [beliefs]; what other agents told
 * stays. The usual update for a perception.
 */
fun replaceSelf(queries: List<Struct>, beliefs: Iterable<PrologBelief>): AgentUpdate.Replace<PrologBelief> =
    replacementFrom(self, queries, beliefs)

/**
 * Updates the agent's own belief with the functor and arity of [belief] to [belief], as Jason's `-+`: updating
 * `counter(4)` forgets the agent's own `counter(_)` and believes `counter(4)[source(self)]`, while the `counter(_)`
 * that other agents told stay. It goes through the revision's replacement, as [replaceSelf] does for perceptions.
 */
fun MutableAgentState<PrologBelief, *>.update(belief: PrologBelief) {
    val head = belief.head
    val sameFunctorAndArity = Struct.of(head.functor, List(head.arity) { Var.anonymous() })
    val replacement = replacementFrom(self, listOf(sameFunctorAndArity), listOf(belief))
    replace(replacement.scope, replacement.replacements)
}

private fun replacementFrom(
    from: Term,
    queries: List<Struct>,
    beliefs: Iterable<PrologBelief>,
): AgentUpdate.Replace<PrologBelief> {
    require(queries.none { it.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty().isNotEmpty() }) {
        "The queries of a replacement from $from must not have annotations, but got $queries"
    }
    val annotation = source(from)
    val scope = queries.map { it.tag(annotation) }
    // of a belief matching a query, only what [from] contributed is in scope
    return AgentUpdate.Replace(
        { belief ->
            val inScope = scope.any { query -> belief.matchBelief(query) != null }
            belief.withAnnotations(setOf(annotation)).takeIf { inScope }
        },
        beliefs.map { it.withAnnotations(setOf(annotation)) },
    )
}

/**
 * Forgets the beliefs matching [beliefQuery], through the revision's replacement. A query without annotations forgets
 * them whatever their annotations: `p(X)` forgets every `p(_)`. A query with annotations forgets only those:
 * `p(X)[source(alice)]` forgets what Alice told about `p(_)`, while what others told stays.
 * @throws IllegalArgumentException if the annotations of [beliefQuery] are not ground: to forget whatever the
 * annotations, leave them out.
 */
fun MutableAgentState<PrologBelief, *>.forgetAllMatching(beliefQuery: Struct) {
    val annotations = beliefQuery.getTag<Set<Struct>>(JAKTA_ANNOTATIONS_TAG).orEmpty()
    require(annotations.all { it.isGround }) {
        "The annotations of a query to forget must be ground, but got $annotations: " +
            "leave them out to forget whatever the annotations"
    }
    replace(
        { belief ->
            when {
                belief.matchBelief(beliefQuery) == null -> null
                annotations.isEmpty() -> belief
                else -> belief.withAnnotations(annotations)
            }
        },
        emptyList(),
    )
}

/**
 * The sources of the beliefs matching [beliefQuery]: the agents that told them, and `self` for the agent's own.
 * Unlike a `[source(S)]` pattern, which binds `S` to a single source, it collects them all.
 */
fun Collection<PrologBelief>.sourcesOf(beliefQuery: Struct): Set<Term> = filter { it.matchBelief(beliefQuery) != null }
    .flatMap { it.annotations }
    .filter { it.functor == "source" && it.arity == 1 }
    .map { it[0] }
    .toSet()
