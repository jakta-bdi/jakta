package it.unibo.jakta.logic

import it.unibo.jakta.self
import it.unibo.jakta.source
import it.unibo.tuprolog.core.*
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.unify.AbstractUnificator

/**
 * Custom unificator that handles annotations in Prolog terms.
 * It extends the AbstractUnificator and overrides the mgu method to consider annotations during unification.
 */
private val annotationUnificator = object : AbstractUnificator() {
    override fun checkTermsEquality(first: Term, second: Term): Boolean = first == second

    override fun mgu(term1: Term, term2: Term, occurCheckEnabled: Boolean): Substitution {
        var fact = annotatedTerm(term1)
        var query = annotatedTerm(term2)

        //TODO this is a workaround that might cause problems in the future
        if(!term1.isFact) {
            fact = annotatedTerm(term2)
            query = annotatedTerm(term1)
        }

        var result = super.mgu(fact, query, occurCheckEnabled)

        if (result !is Substitution.Fail) {
            result = unifyTags(fact, query, result, occurCheckEnabled)
        }

        return result
    }

    /**
     * Extract the term for which to check annotations.
     */
    private fun annotatedTerm(term: Term): Term = when(term) {
        is Rule -> term.head
        else -> term
    }

    @Suppress("UNCHECKED_CAST")
    private fun unifyTags(
        fact: Term,
        query: Term,
        initialResult: Substitution,
        occurCheckEnabled: Boolean,
    ): Substitution {
        val factAnnotations =
            (fact.tags["jakta.annotations"] as? Set<Struct>).orEmpty().toMutableList()

        // every unannotated struct is considered annotated with source(self)
        if (factAnnotations.isEmpty()) {
            factAnnotations.add(source(self))
        }

        val queryAnnotations =
            (query.tags["jakta.annotations"] as? Set<Struct>).orEmpty().toMutableList()

        val result = when {
            queryAnnotations.isEmpty() -> initialResult

            queryAnnotations.size > factAnnotations.size -> Substitution.failed()

            else -> matchAnnotations(
                factAnnotations,
                queryAnnotations,
                initialResult,
                occurCheckEnabled,
            )
        }

        return result
    }

    private fun matchAnnotations(
        factAnnotations: MutableList<Struct>,
        queryAnnotations: MutableList<Struct>,
        initialResult: Substitution,
        occurCheckEnabled: Boolean,
    ): Substitution {
        var result = initialResult

        for (annotation in queryAnnotations) {
            val factIterator = factAnnotations.listIterator()
            var matched = false

            while (factIterator.hasNext()) {
                val candidate = factIterator.next()
                val annotationResult = super.mgu(annotation, candidate, occurCheckEnabled)

                if (annotationResult !is Substitution.Fail) {
                    result += annotationResult
                    factIterator.remove()
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
 * An object representing a way to access a [Solver] configured with JaKtA specific options.
 */
object JaktaSolver {
    /**
     * Factory method to create a new solver instance with a loaded theory.
     */
    fun get(theory: Iterable<Clause>): Solver = Solver.prolog
        .newBuilder()
        .unificator(annotationUnificator)
        .staticKb(theory)
        .flag(Unknown, Unknown.FAIL)
        .flag(TrackVariables) { ON }
        .build()
}

/**
 * Extension function to check if a collection of Prolog rules unifies with a given query [Struct].
 * @param query The [Struct] to be unified with the collection of rules.
 * @return The [Solution] of the unification process, which can be a success or failure.
 */
fun Collection<Rule>.unifiesWith(query: Struct): Solution = JaktaSolver.get(this).solveOnce(query)


/**
* Extension function to compute the most general unifier (MGU) between a [Struct] and a query [Struct]
 * using annotation semantics.
 */
fun Struct.annotatedMguWith(query: Struct): Substitution = annotationUnificator.mgu(this, query)

/**
 * Extension function to check if a collection of Prolog rules unifies with a given query [Struct].
 * @param query The [Struct] to be unified with the collection of rules.
 * @return The [Solution] of the unification process, which can be a success or failure.
 */
fun Collection<Rule>.allSolutionsOf(query: Struct): List<Solution> = JaktaSolver.get(this).solveList(query)

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
