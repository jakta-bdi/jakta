package it.unibo.jakta.agents.bdi.dsl.plans

import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.events.AchievementGoalTrigger
import it.unibo.jakta.agents.bdi.events.BeliefBaseRevision
import it.unibo.jakta.agents.bdi.events.TestGoalTrigger
import it.unibo.jakta.agents.bdi.plans.Plan
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class PlansScope : Builder<Iterable<Plan>>, LogicProgrammingScope by LogicProgrammingScope.empty() {

    private val plans = mutableListOf<PlanScope>()

    fun achieve(goal: String): PlanScope = achieve(atomOf(goal))

    fun achieve(goal: Struct): PlanScope = PlanScope(this, goal, AchievementGoalTrigger::class)

    fun test(goal: String): PlanScope = test(atomOf(goal))

    fun test(goal: Struct): PlanScope = PlanScope(this, goal, TestGoalTrigger::class)

    operator fun String.unaryPlus(): PlanScope {
        val planScope = PlanScope(this@PlansScope, atomOf(this), BeliefBaseRevision::class)
        planScope.failure = false
        plans += planScope
        return planScope
    }

    operator fun Struct.unaryPlus(): PlanScope {
        val planScope = PlanScope(this@PlansScope, this, BeliefBaseRevision::class)
        planScope.failure = false
        plans += planScope
        return planScope
    }

    operator fun PlanScope.unaryPlus(): PlanScope {
        failure = false
        plans += this
        return this
    }

    operator fun String.unaryMinus(): PlanScope {
        val planScope = PlanScope(this@PlansScope, atomOf(this), BeliefBaseRevision::class)
        planScope.failure = true
        plans += planScope
        return planScope
    }

    operator fun Struct.unaryMinus(): PlanScope {
        val planScope = PlanScope(this@PlansScope, this, BeliefBaseRevision::class)
        planScope.failure = true
        plans += planScope
        return planScope
    }

    operator fun PlanScope.unaryMinus(): PlanScope {
        failure = true
        plans += this
        return this
    }

    override fun build(): Iterable<Plan> = plans.map { it.build() }
}
