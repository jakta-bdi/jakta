package io.github.anitvam.agents.bdi.dsl.plans

import io.github.anitvam.agents.bdi.Jacop
import io.github.anitvam.agents.bdi.dsl.Builder
import io.github.anitvam.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Scope

class PlansScope : Builder<Iterable<Plan>> {

    private val plans = mutableListOf<Plan>()
    private val scope = Scope.empty()

    fun achieve(goal: String): TriggerScope {
        return TriggerScope(scope, Jacop.parseStruct(goal))
    }

    operator fun TriggerGuardScope.unaryPlus() {
        plans += build(failure = false)
    }

    operator fun TriggerGuardScope.unaryMinus() {
        plans += build(failure = true)
    }

    override fun build(): Iterable<Plan> = plans.toList()
}
