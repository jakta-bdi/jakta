package it.unibo.jakta.plans.impl

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.events.ASEvent
import it.unibo.jakta.events.BeliefBaseRevision
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.Goal
import it.unibo.jakta.plans.ActivationRecord
import it.unibo.jakta.plans.Guard
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.ASPlan
import it.unibo.jakta.plans.Task
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImp(
    override val trigger: ASEvent,
    override val guard: Struct,
    override val tasks: List<ASTask<*>>,
) : ASPlan {

    override fun isApplicable(event: Event, beliefBase: BeliefBase<Struct, PrologBelief>): Boolean {
        if (event.javaClass != trigger.javaClass || beliefBase.javaClass.isInstance(PrologBeliefBase::class.java))
            return false
        // TODO("Migliorabile per i cast")
        val castedEvent : ASEvent = event as ASEvent
        val castedBB : PrologBeliefBase = beliefBase as PrologBeliefBase
        val mgu = castedEvent.trigger mguWith this.trigger.trigger
        val actualGuard = guard.apply(mgu).castToStruct()
        return isRelevant(event) && castedBB.select(actualGuard).isNotEmpty()
    }

    override fun applicablePlan(event: ASEvent, beliefBase: PrologBeliefBase): ASPlan =
        when (isApplicable(event, beliefBase)) {
            true -> {
                val mgu = event.trigger mguWith this.trigger.trigger
                val actualGuard = guard.apply(mgu).castToStruct()
                val solvedGuard = beliefBase.select(actualGuard)
                val actualGoals = tasks.map { //TODO("Implementare i Task")
                    it.copy(
                        it.value
                            .apply(mgu)
                            .apply(solvedGuard.substitution)
                            .castToStruct(),
                    )
                }

                PlanImpl(event.trigger, actualGuard, actualGoals)
            }
            else -> this
        }

    override fun isRelevant(event: Event<T>): Boolean =
        event.trigger::class == this.trigger::class && (trigger.value mguWith event.trigger.value).isSuccess

    override fun toActivationRecord(): ActivationRecord = ActivationRecord.of(goals, trigger.value)
}
