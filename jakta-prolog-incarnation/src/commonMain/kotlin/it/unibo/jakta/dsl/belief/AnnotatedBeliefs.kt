package it.unibo.jakta.dsl.belief

import it.unibo.jakta.addAnnotations
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.annotations
import it.unibo.jakta.event.AgentUpdate
import it.unibo.jakta.isSource
import it.unibo.jakta.removeAllAnnotations
import it.unibo.jakta.self
import it.unibo.jakta.setAnnotations
import it.unibo.jakta.source
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.core.Var
import it.unibo.tuprolog.unify.Unificator

/**
 * What [from] contributed to the beliefs matching any of [queries] is now [beliefs], attributed to [from]; what other
 * sources contributed stays. The queries must not have annotations. In a perception handler,
 * `replaceFrom(percept, queries, beliefs)` updates what the agent perceives, marked with `source(percept)`.
 *
 * It needs [AnnotatedBeliefRevision] (`usesAnnotatedBeliefs()`), which keeps the sources it reads: an agent that needs
 * no annotations replaces its beliefs with `AgentUpdate.Replace.where`.
 */
fun replaceFrom(
    from: Term,
    queries: List<Struct>,
    beliefs: Iterable<PrologBelief>,
): AgentUpdate.Replace<PrologBelief> {
    require(queries.none { it.annotations.isNotEmpty() }) {
        "The queries of a replacement from $from must not have annotations, but got $queries"
    }
    val annotation = source(from)
    val scope = queries.map { it.addAnnotations(annotation) }
    // of a belief matching a query, only what [from] contributed is in scope
    return AgentUpdate.Replace(beliefs.map { it.addAnnotations(annotation) }) { belief ->
        val inScope = scope.any { query -> belief.matchBelief(query) != null }
        belief.setAnnotations(setOf(annotation)).takeIf { inScope }
    }
}

/**
 * Updates the agent's own belief with the functor and arity of [belief] to [belief], as Jason's `-+`: updating
 * `counter(4)` forgets the agent's own `counter(_)` and believes `counter(4)[source(self)]`, while the `counter(_)`
 * that other agents told stay. It goes through the revision's replacement, as [replaceFrom] does for perceptions.
 */
fun MutableAgentState<PrologBelief, *>.update(belief: PrologBelief) {
    val head = belief.head
    val sameFunctorAndArity = Struct.of(head.functor, List(head.arity) { Var.anonymous() })
    val replacement = replaceFrom(self, listOf(sameFunctorAndArity), listOf(belief))
    replace(replacement.replacements, replacement.scope)
}

/**
 * Forgets every solution of [beliefQuery], through the revision's replacement. Without annotations, the query forgets
 * the matching beliefs whole: `p(X)` forgets every `p(_)`. With annotations, it forgets from each matching belief the
 * annotations the query's ones match, and the belief goes with its last source: with `p(1)[source(alice),
 * source(bob)]`, `p(X)[source(alice)]` leaves `p(1)[source(bob)]`, and `p(X)[source(S)]` forgets `p(1)`.
 *
 * The query's annotations are matched after the bindings of the belief's term, each on its own: variables shared by two
 * of them are not required to agree.
 */
fun MutableAgentState<PrologBelief, *>.forgetAllMatching(beliefQuery: Struct) = replace(emptyList()) { belief ->
    val termMatch = belief.matchBelief(beliefQuery.removeAllAnnotations())
    when {
        termMatch == null || belief.matchBelief(beliefQuery) == null -> null

        beliefQuery.annotations.isEmpty() -> belief

        else -> {
            val patterns = beliefQuery.annotations.map { it.apply(termMatch.substitution) }
            val matched = belief.annotations.filter {
                patterns.any { pattern -> pattern.matches(it) }
            }
            belief.setAnnotations(matched.toSet())
        }
    }
}

private fun Term.matches(annotation: Struct) = Unificator.default.match(this, annotation)

/**
 * The sources of the beliefs matching [beliefQuery]: the agents that told them, and `self` for the agent's own, as
 * [AnnotatedBeliefRevision] stores them.
 * Unlike a `[source(S)]` pattern, which binds `S` to a single source, it collects them all.
 */
fun Collection<PrologBelief>.sourcesOf(beliefQuery: Struct): Set<Term> = filter { it.matchBelief(beliefQuery) != null }
    .flatMap { it.annotations }
    .filter { it.isSource }
    .map { it[0] }
    .toSet()
