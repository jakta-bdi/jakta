package it.unibo.jakta.logic

import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.unify.AbstractUnificator

/**
 * Custom unificator that handles annotations in Prolog terms.
 * It extends the AbstractUnificator and overrides the mgu method to consider annotations during unification.
 */
val annotationUnificator = object : AbstractUnificator() {
    override fun checkTermsEquality(first: Term, second: Term): Boolean = first == second

    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        var result = super.mgu(term1, term2, occurCheckEnabled)

        if (result !is Substitution.Fail) {
            result = unifyTags(term1, term2, result, occurCheckEnabled)
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    private fun unifyTags(
        query: Term,
        fact: Term,
        initialResult: Substitution,
        occurCheckEnabled: Boolean,
    ): Substitution {
        val queryAnnotations =
            (query.tags["jakta.annotations"] as? Set<Struct>).orEmpty().toMutableList()

        val factAnnotations =
            (fact.tags["jakta.annotations"] as? Set<Struct>).orEmpty().toMutableList()

        val result = when {
            queryAnnotations.isEmpty() -> initialResult

            queryAnnotations.size > factAnnotations.size -> Substitution.failed()

            else -> matchAnnotations(
                queryAnnotations,
                factAnnotations,
                initialResult,
                occurCheckEnabled,
            )
        }

        return result
    }

    private fun matchAnnotations(
        queryAnnotations: MutableList<Struct>,
        factAnnotations: MutableList<Struct>,
        initialResult: Substitution,
        occurCheckEnabled: Boolean,
    ): Substitution {
        var result = initialResult

        for (annotation in factAnnotations) {
            val queryIterator = queryAnnotations.listIterator()
            var matched = false

            while (queryIterator.hasNext()) {
                val candidate = queryIterator.next()
                val annotationResult = super.mgu(candidate, annotation, occurCheckEnabled)

                if (annotationResult !is Substitution.Fail) {
                    result += annotationResult
                    queryIterator.remove()
                    matched = true
                    break
                }
            }

            if (!matched) {
                return Substitution.failed()
            }
        }

        return result
    }
}

/**
 * Extension function to check if a collection of Prolog rules unifies with a given query [Struct].
 * @param query The [Struct] to be unified with the collection of rules.
 * @return The [Solution] of the unification process, which can be a success or failure.
 */
fun Collection<Rule>.unifiesWith(query: Struct): Solution = Solver.prolog
    .newBuilder()
    .unificator(annotationUnificator)
    .flag(Unknown, Unknown.FAIL)
    .staticKb(this)
    .flag(TrackVariables) { ON }
    .build()
    .solveOnce(query)

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
 * Checks if the given [Struct] is ground (i.e., contains no variables).
 * @param struct The [Struct] to be checked.
 * @param message A lambda function that generates an error message if the check fails.
 */
fun requireGround(struct: Struct, message: (Struct) -> String = { "Struct $it must be ground" }) =
    require(struct.isGround) { message(struct) }
