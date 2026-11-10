package it.unibo.jakta.dsl.belief

import it.unibo.jakta.addAnnotations
import it.unibo.jakta.agent.MutableAgentState
import it.unibo.jakta.annotations
import it.unibo.jakta.effectiveAnnotations
import it.unibo.jakta.event.AgentUpdate
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
 * `replaceFrom(self, queries, beliefs)` updates what the agent perceives as its own beliefs, while
 * `replaceFrom(Atom.of("percept"), queries, beliefs)` marks it with `source(percept)`.
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
    return AgentUpdate.Replace(
        { belief ->
            val inScope = scope.any { query -> belief.matchBelief(query) != null }
            belief.setAnnotations(annotation).takeIf { inScope }
        },
        beliefs.map { it.addAnnotations(annotation) },
    )
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
    replace(replacement.scope, replacement.replacements)
}

/**
 * Forgets every solution of [beliefQuery], through the revision's replacement. A query without annotations forgets the
 * matching beliefs whole: `p(X)` forgets every `p(_)`. A query with annotations forgets, from each matching belief, the
 * annotations its patterns match: `p(X)[source(alice)]` forgets what Alice told about `p(_)`, while what others told
 * stays, and `p(X)[source(S)]` forgets every source of every `p(_)`.
 *
 * Each annotation pattern is matched on its own, after the bindings of the belief's term: `p(X)[source(X)]` forgets
 * `source(1)` from `p(1)`, but variables shared by two patterns are not required to agree.
 */
fun MutableAgentState<PrologBelief, *>.forgetAllMatching(beliefQuery: Struct) {
    val patterns = beliefQuery.annotations
    val anyAnnotations = beliefQuery.removeAllAnnotations()
    replace(
        { belief ->
            val termMatch = belief.matchBelief(anyAnnotations)
            when {
                termMatch == null || belief.matchBelief(beliefQuery) == null -> null

                patterns.isEmpty() -> belief

                else -> {
                    val bound = patterns.map { it.apply(termMatch.substitution) }
                    val forgotten = belief.effectiveAnnotations.filter { annotation ->
                        bound.any { Unificator.default.match(it, annotation) }
                    }
                    belief.setAnnotations(*forgotten.toTypedArray())
                }
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
    .flatMap { it.effectiveAnnotations }
    .filter { it.functor == "source" && it.arity == 1 }
    .map { it[0] }
    .toSet()
