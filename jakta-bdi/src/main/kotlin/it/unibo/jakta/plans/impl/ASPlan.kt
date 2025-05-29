package it.unibo.jakta.plans.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.value
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

data class ASPlan(
    override val trigger: Struct,
    override val guard: Struct,
    val tasks: List<ASAction>,
) : Plan<ASBelief, Struct, Solution> {

    override fun isApplicable(event: Event.Internal, beliefBase: BeliefBase<ASBelief, Struct, Solution>): Boolean {
        val toMatch: Struct = event.value
        val mgu = toMatch mguWith this.trigger
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && beliefBase.select(actualGuard).isYes
    }

    override fun toString(): String {
        val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
        val t = formatter.format(trigger)
        val g = formatter.format(guard)
        val body = tasks.joinToString("; ") { it.toString() }
        return "+!$t : $g <- $body\n"
    }

    override fun applicablePlan(
        event: Event.Internal,
        beliefBase: BeliefBase<ASBelief, Struct, Solution>,
    ): Plan<ASBelief, Struct, Solution> =
        when (isApplicable(event, beliefBase)) {
            true -> {
                val mgu = event.value mguWith this.trigger
                val actualGuard = guard.apply(mgu).castToStruct()
                val solvedGuard = beliefBase.select(actualGuard)
                ASPlan(
                    trigger = event.value,
                    guard = actualGuard,
                    tasks = tasks.map {
                        it.applySubstitution(mgu).applySubstitution(solvedGuard.substitution)
                    },
                )
            }
            else -> this
        }

    override fun isRelevant(event: Event.Internal): Boolean =
        event::class == this.trigger::class && (trigger mguWith event.value).isSuccess

    override fun toActivationRecord(): ASActivationRecord = ASActivationRecord(this, tasks.asSequence())
    override fun apply(event: Event): List<ASAction> = tasks
}
