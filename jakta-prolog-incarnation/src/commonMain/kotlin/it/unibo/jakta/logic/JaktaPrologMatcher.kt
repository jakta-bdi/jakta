package it.unibo.jakta.logic

import it.unibo.jakta.self
import it.unibo.jakta.source
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.unify.AbstractUnificator

/**
 * Matches a term against annotated data, as plan triggers do: [term1] is the query (e.g. a plan trigger) and [term2]
 * the annotated data it is matched against (e.g. a belief, or the goal of an event). Each annotation of the query
 * matches a different annotation of the data, with backtracking; data without annotations counts as `[source(self)]`,
 * and a query without annotations matches data with any.
 */
private val annotationUnificator = object : AbstractUnificator() {
    override fun checkTermsEquality(first: Term, second: Term): Boolean = first == second

    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        val query = annotatedTerm(term1)
        val data = annotatedTerm(term2)
        // the data first, as in solving: its variables are bound to the query's terms
        val result = super.mgu(data, query, occurCheckEnabled)
        return when {
            result is Substitution.Fail || query.rawAnnotations.isEmpty() -> result

            else -> matchAnnotations(
                query.rawAnnotations.toList(),
                data.rawAnnotations.ifEmpty { setOf(source(self)) }.toList(),
                result,
                occurCheckEnabled,
            )
        }
    }

    private fun annotatedTerm(term: Term): Term = when (term) {
        is Rule -> term.head
        else -> term
    }

    private fun matchAnnotations(
        query: List<Struct>,
        data: List<Struct>,
        result: Substitution,
        occurCheckEnabled: Boolean,
    ): Substitution {
        if (query.isEmpty()) return result
        val annotation = query.first().apply(result)
        return data.indices.asSequence().map { i ->
            val matched = super.mgu(annotation, data[i], occurCheckEnabled)
            val combined = if (matched is Substitution.Fail) matched else result + matched
            when (combined) {
                is Substitution.Fail -> combined
                else -> matchAnnotations(query.drop(1), data - data[i], combined, occurCheckEnabled)
            }
        }.firstOrNull { it !is Substitution.Fail } ?: Substitution.failed()
    }
}

/**
 * The first solution of [query] on these beliefs and rules. Annotations are solved like the rest of the terms: a
 * variable in an annotation, such as `S` in `p(X)[source(S)]`, is bound by the solution.
 * @param query The [Struct] to be solved on the collection of rules.
 * @return The [Solution] of the query, which can be a success or failure.
 */
fun Collection<Rule>.unifiesWith(query: Struct): Solution = solveOnce(query)

/**
 * Computes the most general unifier (MGU) between this annotated data, as a belief or a goal, and the [query] of a plan
 * trigger: each annotation of the query must match a different annotation of the data.
 */
fun Struct.annotatedMguWith(query: Struct): Substitution = annotationUnificator.mgu(query, this)

/**
 * Every solution of [query] on these beliefs and rules, as for [unifiesWith].
 * @param query The [Struct] to be solved on the collection of rules.
 * @return The [Solution]s of the query.
 */
fun Collection<Rule>.allSolutionsOf(query: Struct): List<Solution> = solveAll(query)

/**
 * List of special functors in Prolog that are not considered predicates.
 */
val specialFunctors = listOf(".", "{}", ":-", "/", "[]", ",", ";", "\\+", "&", "|")

/**
 * Checks if the given [Struct] is a predicate by ensuring its functor is not in the list of special functors.
 * @param struct The [Struct] to be checked.
 * @param message A lambda function that generates an error message if the check fails.
 */
fun requirePredicate(struct: Struct, message: (Struct) -> String = { "Struct $it must be a predicate" }) =
    require(struct.functor !in specialFunctors) { message(struct) }

/**
 * Checks if the given [Struct] is ground (i.e., contains no variables, in its annotations neither).
 * @param struct The [Struct] to be checked.
 * @param message A lambda function that generates an error message if the check fails.
 */
fun requireGround(struct: Struct, message: (Struct) -> String = { "Struct $it must be ground" }) =
    require(struct.allVariables().isEmpty()) { message(struct) }
