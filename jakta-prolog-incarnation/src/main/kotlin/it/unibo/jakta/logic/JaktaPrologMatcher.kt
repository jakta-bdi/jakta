package it.unibo.jakta.logic

import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown

/**
 * Extension function to check if a collection of Prolog rules unifies with a given query [Struct].
 * @param query The [Struct] to be unified with the collection of rules.
 * @return The [Solution] of the unification process, which can be a success or failure.
 */
fun Collection<Rule>.unifiesWith(query: Struct): Solution = Solver.prolog
    .newBuilder()
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
