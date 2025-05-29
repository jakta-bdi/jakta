package it.unibo.jakta.plans.impl

import it.unibo.jakta.Jakta
import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.TermFormatter
import it.unibo.tuprolog.core.operators.OperatorSet
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override val trigger: ASEvent,
    override val guard: Struct,
    override val tasks: List<ASAction>,
) : ASPlan {

    override fun isApplicable(event: ASEvent, beliefBase: ASBeliefBase): Boolean {
        val castedEvent: ASEvent = event
        val mgu = castedEvent.value mguWith this.trigger.value
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && beliefBase.select(actualGuard).isNotEmpty()
    }

    override fun toString(): String {
        val formatter = TermFormatter.prettyExpressions(operatorSet = OperatorSet.DEFAULT + Jakta.operators)
        val t = formatter.format(trigger.value)
        val g = formatter.format(guard)
        val body = tasks.joinToString("; ") { it.toString() }
        return "+!$t : $g <- $body\n"
    }

    override fun applicablePlan(event: ASEvent, beliefBase: ASBeliefBase): ASPlan =
        when (isApplicable(event, beliefBase)) {
            true -> {
                val mgu = event.value mguWith this.trigger.value
                val actualGuard = guard.apply(mgu).castToStruct()
                val solvedGuard = beliefBase.getSolutionOf(actualGuard)
                PlanImpl(
                    trigger = event,
                    guard = actualGuard,
                    tasks = tasks.map {
                        it.applySubstitution(mgu).applySubstitution(solvedGuard.substitution)
                    },
                )
            }
            else -> this
        }

    override fun isRelevant(event: ASEvent): Boolean =
        event::class == this.trigger::class && (trigger.value mguWith event.value).isSuccess

    override fun toActivationRecord(): ASActivationRecord = ASActivationRecord(this, tasks)
    override fun apply(event: Event): List<Action> = tasks
}
