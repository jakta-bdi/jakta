package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import io.github.anitvam.agents.bdi.events.Event
import io.github.anitvam.agents.bdi.events.Trigger
import io.github.anitvam.agents.bdi.goals.Goal
import it.unibo.tuprolog.core.Struct

interface Plan {
    val id: PlanID
    val event: Trigger
    val guard: Struct
    val goals: List<Goal>

    fun isApplicable(event: Event, beliefBase: BeliefBase): Boolean

    fun toActivationRecord(): ActivationRecord
}
