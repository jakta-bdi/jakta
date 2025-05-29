package it.unibo.jakta.dsl.goals

import it.unibo.jakta.dsl.Builder
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.TestGoalInvocation
import it.unibo.jakta.events.Trigger
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.jakta.dsl.JaktaLogicProgrammingScope

class InitialGoalsScope :
    Builder<Iterable<Trigger>>,
    JaktaLogicProgrammingScope by JaktaLogicProgrammingScope.empty() {
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
