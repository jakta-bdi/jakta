package io.github.anitvam.agents.bdi.plans

import io.github.anitvam.agents.bdi.plans.Plan

interface PlanLibrary {
    val plans: Set<Plan>
}
