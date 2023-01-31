package io.github.anitvam.agents.bdi.dsl

import io.github.anitvam.agents.bdi.plans.Plan
import io.github.anitvam.agents.bdi.plans.PlanLibrary
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct

class PlansScope : Builder<PlanLibrary> {

    private val plans = mutableListOf<Plan>()
    private val scope = Scope.empty()

    fun achieve(goal: Struct): TriggerScope {
        return TriggerScope(scope, goal)
    }

    operator fun TriggerGuardScope.unaryPlus() {
        plans += build(failure = false)
    }

    operator fun TriggerGuardScope.unaryMinus() {
        plans += build(failure = true)
    }

    override fun build(): PlanLibrary {
        TODO("Not yet implemented")
    }
}
