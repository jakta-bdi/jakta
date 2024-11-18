package it.unibo.jakta.plans.impl

import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.intentions.ActivationRecord
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Task
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override val trigger: ASEvent,
    override val guard: Struct,
    override val tasks: List<Task<*>>,
) : ASPlan {

    override fun isApplicable(event: Event, beliefBase: BeliefBase<Struct, ASBelief>): Boolean {
        if (event.javaClass != trigger.javaClass || beliefBase.javaClass.isInstance(ASBeliefBase::class.java)) {
            return false
        }
        // TODO("Migliorabile per i cast")
        val castedEvent: ASEvent = event as ASEvent
        val castedBB: ASBeliefBase = beliefBase as ASBeliefBase
        val mgu = castedEvent.trigger mguWith this.trigger.trigger
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && castedBB.select(actualGuard).isNotEmpty()
    }

    override fun applicablePlan(event: ASEvent, beliefBase: ASBeliefBase): ASPlan =
        when (isApplicable(event, beliefBase)) {
            true -> {
                val mgu = event.trigger mguWith this.trigger.trigger
                val actualGuard = guard.apply(mgu).castToStruct()
                val solvedGuard = beliefBase.select(actualGuard)
                val actualGoals = tasks.map { // TODO("Implementare i Task")
//                    it.copy(
                        it.value
                            .apply(mgu)
                            .apply(solvedGuard.substitution)
                            .castToStruct(),
//                    )
                }

                PlanImpl(event.trigger, actualGuard, actualGoals)
            }
            else -> this
        }

    override fun isRelevant(event: Event<T>): Boolean =
        event.trigger::class == this.trigger::class && (trigger.value mguWith event.trigger.value).isSuccess

    override fun toActivationRecord(): ActivationRecord = ActivationRecord.of(goals, trigger.value)
}
