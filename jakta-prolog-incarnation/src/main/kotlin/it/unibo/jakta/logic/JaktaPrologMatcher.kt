package it.unibo.jakta.logic

import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown

fun Collection<Rule>.unifiesWith(belief: Struct): Solution = Solver.prolog
    .newBuilder()
    .flag(Unknown, Unknown.FAIL)
    .staticKb(this)
    .flag(TrackVariables) { ON }
    .build()
    .solveOnce(belief)

val specialFunctors = listOf(".", "{}", ":-", "/", "[]", ",", ";", "\\+", "&", "|")

fun requirePredicate(struct: Struct, message: (Struct) -> String = { "Struct $it must be a predicate" }) =
    require(struct.functor !in specialFunctors) { message(struct) }

fun requireGround(struct: Struct, message: (Struct) -> String = { "Struct $it must be ground" }) =
    require(struct.isGround) { message(struct) }
