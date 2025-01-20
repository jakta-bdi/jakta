package it.unibo.jakta.plans.impl

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ASActivationRecord
import it.unibo.jakta.plans.ASPlan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override val trigger: ASEvent,
    override val guard: Struct,
    override val tasks: List<ASAction>,
) : ASPlan {

    override fun isApplicable(event: ASEvent, beliefBase: BeliefBase<Struct, ASBelief>): Boolean {
        if (event.javaClass != trigger.javaClass || beliefBase.javaClass.isInstance(ASBeliefBase::class.java)) {
            return false
        }
        // TODO("Migliorabile per i cast")
        val castedEvent: ASEvent = event as ASEvent
        val castedBB: ASBeliefBase = beliefBase as ASBeliefBase
        val mgu = castedEvent.value mguWith this.trigger.value
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && castedBB.select(actualGuard).isNotEmpty()
    }

    override fun applicablePlan(event: ASEvent, beliefBase: ASBeliefBase): ASPlan =
        when (isApplicable(event, beliefBase)) {
            true -> {
                val mgu = event.value mguWith this.trigger.value
                val actualGuard = guard.apply(mgu).castToStruct()
                val solvedGuard = beliefBase.select(actualGuard)
                val actualGoals = tasks.map { // TODO("Does the new Task structure lose the concept of unification?")
//                    it.copy(
//                        it.value
//                            .apply(mgu)
//                            .apply(solvedGuard.substitution)
//                            .castToStruct(),
//                    )
                }

                //PlanImpl(event.value, actualGuard, actualGoals)
                this
            }
            else -> this
        }

    override fun isRelevant(event: ASEvent): Boolean =
        event::class == this.trigger::class && (trigger.value mguWith event.value).isSuccess

    override fun toActivationRecord(): ASActivationRecord = ASActivationRecord(this, tasks)
}
