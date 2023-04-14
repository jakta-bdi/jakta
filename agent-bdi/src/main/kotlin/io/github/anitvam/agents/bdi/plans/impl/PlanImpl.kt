package io.github.anitvam.agents.bdi.plans.impl

import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override val trigger: Trigger,
    override val guard: Struct,
    override val goals: List<Goal>,
) : Plan {
    override fun isApplicable(event: Event, beliefBase: BeliefBase): Boolean {
        val mgu = event.trigger.value mguWith this.trigger.value
        val actualGuard = guard.apply(mgu).castToStruct()
//        println("guard: $actualGuard")
//        println("BB: ${ beliefBase.joinToString("\n") }")
//        println("SOLUTION: ${beliefBase.solve(actualGuard)} ")
        val ex = beliefBase.solve(actualGuard)
        if ((ex.exception != null)) {
            println("LOGIC STACK TRACE: ${ ex.exception?.logicStackTrace}")
        }
        println()
        return isRelevant(event) && beliefBase.solve(actualGuard).isYes
    }

    override fun applicablePlan(event: Event, beliefBase: BeliefBase): Plan = when (isApplicable(event, beliefBase)) {
        true -> {
            val mgu = event.trigger.value mguWith this.trigger.value
            val actualGuard = guard.apply(mgu).castToStruct()
            val solvedGuard = beliefBase.solve(actualGuard)
            val actualGoals = goals.map {
                it.copy(
                    it.value
                        .apply(mgu)
                        .apply(solvedGuard.substitution)
                        .castToStruct()
                )
            }

            PlanImpl(event.trigger, actualGuard, actualGoals)
        }
        else -> this
    }

    override fun isRelevant(event: Event): Boolean =
        event.trigger::class == this.trigger::class && (trigger.value mguWith event.trigger.value).isSuccess

    override fun toActivationRecord(): ActivationRecord = ActivationRecord.of(goals, trigger.value)
}
