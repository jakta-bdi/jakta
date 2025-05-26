package it.unibo.jakta.plans.impl

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.Action
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.Event
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override var trigger: ASEvent,
    override var guard: Struct,
    val tasks: List<ASAction>,
) : ASPlan {

    override fun isApplicable(event: ASEvent, beliefBase: ASBeliefBase): Boolean {
        val castedEvent: ASEvent = event
        val mgu = castedEvent.value mguWith this.trigger.value
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && beliefBase.select(actualGuard).isNotEmpty()
    }

    override fun applicablePlan(event: ASEvent, beliefBase: ASBeliefBase): ASPlan =
        when (isApplicable(event, beliefBase)) {
            true -> {
                val mgu = event.value mguWith this.trigger.value
                val actualGuard = guard.apply(mgu).castToStruct()
                val solvedGuard = beliefBase.getSolutionOf(actualGuard)
                tasks.forEach {
                    it.applySubstitution(mgu)
                    it.applySubstitution(solvedGuard.substitution)
                }

                this.trigger = event
                this.guard = actualGuard
                this
            }
            else -> this
        }

    override fun isRelevant(event: ASEvent): Boolean =
        event::class == this.trigger::class && (trigger.value mguWith event.value).isSuccess

    override fun toActivationRecord(): ASActivationRecord = ASActivationRecord(this, tasks)
    override fun apply(event: Event): List<Action> = tasks
}
