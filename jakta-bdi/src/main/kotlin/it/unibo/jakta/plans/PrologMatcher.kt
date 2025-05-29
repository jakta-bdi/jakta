package it.unibo.jakta.plans

import it.unibo.jakta.Jakta
import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.value
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

object PrologMatcher : Matcher<ASBelief, Struct, Solution> {

    override fun query(query: Struct, base: BeliefBase<ASBelief, Struct, Solution>): Solution =
        Solver.prolog.newBuilder()
            .flag(Unknown, Unknown.FAIL)
            .staticKb(operatorExtension + Theory.of(base.map { it.content }))
            .flag(TrackVariables) { ON }
            .build()
            .solveOnce(query)

    override fun matchPlanFor(
        event: Event.Internal,
        plans: Collection<Plan<ASBelief, Struct, Solution>>,
        beliefBase: BeliefBase<ASBelief, Struct, Solution>,
    ): ActivationRecord<ASBelief, Struct, Solution>? = plans.asSequence()
        // Relevant plans
        .filter { it.type.matches(event) }
        .filter { (it.trigger mguWith event.value).isSuccess }
        // First applicable plan
        .map { plan ->
            val mgu = event.value mguWith plan.trigger
            val actualGuard = plan.guard.apply(mgu).castToStruct()
            val solvedGuard = beliefBase.select(actualGuard)
            plan to solvedGuard.substitution
        }
        .firstOrNull { (_, substitution) -> substitution.isSuccess }
        ?.let { (plan, substitution) ->
            val substitutedActions: Sequence<ASAction> = plan.actions.asSequence().filterIsInstance<ASAction>()
                .map { it.applySubstitution(substitution) }
            ASActivationRecord(plan, substitutedActions)
        }

    private val operatorExtension = Theory.of(
        Jakta.parseClause("&(A, B) :- A, B"),
        Jakta.parseClause("|(A, _) :- A"),
        Jakta.parseClause("|(_, B) :- B"),
        Jakta.parseClause("~(X) :- not(X)"),
    )
}
