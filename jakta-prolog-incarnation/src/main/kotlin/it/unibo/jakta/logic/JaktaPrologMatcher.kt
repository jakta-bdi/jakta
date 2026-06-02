package it.unibo.jakta.logic

import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator

private fun Collection<Rule>.toClauseMultiSet(): ClauseMultiSet = ClauseMultiSet.of(Unificator.default, this)

fun Collection<Rule>.unifiesWith(belief: Struct): Solution = Solver.prolog
    .newBuilder()
    .flag(Unknown, Unknown.FAIL)
    .staticKb(operatorExtension + Theory.of(this.toClauseMultiSet()))
    .flag(TrackVariables) { ON }
    .build()
    .solveOnce(belief)

// TODO do we need these?
private val operatorExtension = Theory.of(
    JaktaParser.parseClause("&(A, B) :- A, B"),
    JaktaParser.parseClause("|(A, _) :- A"),
    JaktaParser.parseClause("|(_, B) :- B"),
    JaktaParser.parseClause("~(X) :- not(X)"), // TODO this does not seem equivalent.. check with Giovanni
)

val specialFunctors = listOf(".", "{}", ":-", "/", "[]", ",")

fun requirePredicate(struct: Struct, message: (Struct) -> String = { "Struct $it must be a predicate" }) =
    require(struct.functor !in specialFunctors) { message(struct) }

fun requireGround(struct: Struct, message: (Struct) -> String = { "Struct $it must be ground" }) =
    require(struct.isGround) { message(struct) }
