package io.github.anitvam.agents.bdi.plans.impl

import io.github.anitvam.agents.bdi.plans.ActivationRecord
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanID
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.unify.Unificator.Companion.mguWith

internal data class PlanImpl(
    override val id: PlanID,
    override val event: Trigger,
    override val guard: Struct,
    override val goals: List<Goal>
) : Plan {
    override fun isApplicable(event: Event, beliefBase: BeliefBase): Boolean {
        val mgu = event.trigger.value mguWith this.event.value
        val actualGuard = guard.apply(mgu).castToStruct()
        return beliefBase.solve(actualGuard).isYes
    }

    override fun toActivationRecord(): ActivationRecord = ActivationRecord.of(goals)
}
