package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct

class TriggerGuardScope(
    private val scope: Scope,
    private val trigger: Struct,
    private val guard: Struct
) {

    private lateinit var goals: List<Goal>

    infix fun then(body: BodyScope.() -> Unit) {
        goals = BodyScope(scope).build()
    }

    fun build(failure: Boolean): Plan {
        if (failure) {
            TODO()
        } else {
            TODO()
        }
    }
}
