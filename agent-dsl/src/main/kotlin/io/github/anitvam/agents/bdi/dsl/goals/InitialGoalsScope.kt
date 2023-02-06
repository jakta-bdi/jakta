package io.github.anitvam.agents.bdi.dsl.goals

import io.github.anitvam.agents.bdi.dsl.Builder
import io.github.anitvam.agents.bdi.events.AchievementGoalInvocation
import io.github.anitvam.agents.bdi.events.TestGoalInvocation
import io.github.anitvam.agents.bdi.events.Trigger
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class InitialGoalsScope : Builder<Iterable<Trigger>>, LogicProgrammingScope by LogicProgrammingScope.empty() {

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
