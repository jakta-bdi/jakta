package it.unibo.jakta.plans.impl

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase
import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.beliefs.PrologBeliefBase
import it.unibo.jakta.events.BeliefBaseRevision
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.Trigger
import it.unibo.jakta.goals.Goal
import it.unibo.jakta.plans.ActivationRecord
import it.unibo.jakta.plans.Guard
import it.unibo.jakta.plans.Plan
import it.unibo.jakta.plans.PrologPlan
import it.unibo.tuprolog.core.Rule
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl<T : Trigger<*>, Go : Goal<*>>(
    override val trigger: T,
    override val guard: Guard<Struct>,
    override val goals: List<Go>,
) : PrologPlan<T, Go> {

    override fun <B : Belief<*>, C : BeliefBase<B, C>> isApplicable(event: Event<T>, beliefBase: C): Boolean {
        if (event.trigger.javaClass != trigger.javaClass) return false
        if (beliefBase.javaClass != PrologBeliefBase::class.java) return false
        val bb: PrologBeliefBase = beliefBase as PrologBeliefBase // Safe cast
        val mgu = when (event.trigger) {
            is BeliefBaseRevision<*> -> if (event.trigger.value is PrologBelief && this.trigger.value is PrologBelief) {
                (event.trigger.value as PrologBelief).content mguWith (this.trigger.value as PrologBelief).content
            } else {
                Substitution.failed()
            }
            else -> if (event.trigger.value is PrologBelief && this.trigger.value is PrologBelief) {
                (event.trigger.value as Rule) mguWith (this.trigger.value as Rule)
            } else {
                Substitution.failed()
            }
        }
        val actualGuard = guard.expression.apply(mgu).castToStruct()
        return isRelevant(event) && beliefBase.solve(actualGuard).isSuccess
    }

    override fun applicablePlan(event: Event<T>, beliefBase: PrologBeliefBase): Plan = when (
        isApplicable(
            event,
            beliefBase,
        )
    ) {
        true -> {
            val mgu = event.trigger.value mguWith this.trigger.value
            val actualGuard = guard.apply(mgu).castToStruct()
            val solvedGuard = beliefBase.solve(actualGuard)
            val actualGoals = goals.map {
                it.copy(
                    it.value
                        .apply(mgu)
                        .apply(solvedGuard.result.substitution)
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
