package it.unibo.jakta.agents.bdi.dsl.goals

import it.unibo.jakta.agents.bdi.dsl.Builder
import it.unibo.jakta.agents.bdi.events.AchievementGoalInvocation
import it.unibo.jakta.agents.bdi.events.TestGoalInvocation
import it.unibo.jakta.agents.bdi.events.Trigger
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.jakta.JaktaLogicProgrammingScope

class InitialGoalsScope : Builder<Iterable<Trigger>>, JaktaLogicProgrammingScope by JaktaLogicProgrammingScope.empty() {

    private val triggers = mutableListOf<Trigger>()

    fun achieve(goal: Struct) {
        triggers += AchievementGoalInvocation(goal)
    }

    fun achieve(goal: String) {
        triggers += AchievementGoalInvocation(atomOf(goal))
    }

    fun test(goal: Struct) {
        triggers += TestGoalInvocation(goal)
    }

    fun test(goal: String) {
        triggers += TestGoalInvocation(atomOf(goal))
    }

    override fun build(): Iterable<Trigger> = triggers.toList()
}
