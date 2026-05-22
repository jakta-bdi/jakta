package it.unibo.jakta.belief

import it.unibo.jakta.dsl.JaktaLogicProgrammingScope
import it.unibo.jakta.plan.GuardScope
import it.unibo.tuprolog.collections.ClauseMultiSet
import it.unibo.tuprolog.core.Fact
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

typealias PrologBelief = Rule

private fun Collection<PrologBelief>.toClauseMultiSet(): ClauseMultiSet = ClauseMultiSet.of(Unificator.default, this)

private fun Collection<PrologBelief>.unifiesWith(belief: Struct): Solution = Solver.prolog
    .newBuilder()
    .flag(Unknown, Unknown.FAIL)
    .staticKb(operatorExtension + Theory.of(this.toClauseMultiSet()))
    .flag(TrackVariables) { ON }
    .build()
    .solveOnce(belief)

fun PrologBelief.matching(belief: Fact): Substitution? =
    when (val substitution = this mguWith belief) {
        is Substitution.Fail -> null
        else -> substitution
    }

context(scope: JaktaLogicProgrammingScope)
fun <Context: Substitution> GuardScope<PrologBelief, Context>.condition(
    guard: JaktaLogicProgrammingScope.() -> Struct,
): Substitution? {
    val guard = scope.guard()
    val substitutedGuard = guard.apply(this.context).castToStruct()
    return when (val solution = this.beliefs.unifiesWith(substitutedGuard)) {
        is Solution.Yes -> solution.substitution + this.context
        else -> null
    }
}

private val operatorExtension = Theory.of(
    JaktaParser.parseClause("&(A, B) :- A, B"),
    JaktaParser.parseClause("|(A, _) :- A"),
    JaktaParser.parseClause("|(_, B) :- B"),
    JaktaParser.parseClause("~(X) :- not(X)"),
)

val specialFunctors = listOf(".", "{}", ":-", "/", "[]", ",")
