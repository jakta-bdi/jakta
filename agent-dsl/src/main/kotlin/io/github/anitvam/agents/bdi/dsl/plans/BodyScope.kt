package io.github.anitvam.agents.bdi.dsl.plans

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.dsl.Builder
import io.github.anitvam.agents.bdi.goals.Achieve
import io.github.anitvam.agents.bdi.goals.Act
import io.github.anitvam.agents.bdi.goals.ActInternally
import io.github.anitvam.agents.bdi.goals.AddBelief
import io.github.anitvam.agents.bdi.goals.Goal
import io.github.anitvam.agents.bdi.goals.RemoveBelief
import io.github.anitvam.agents.bdi.goals.Spawn
import io.github.anitvam.agents.bdi.goals.Test
import io.github.anitvam.agents.bdi.goals.UpdateBelief
import it.unibo.tuprolog.core.Scope
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.dsl.LogicProgrammingScope

class BodyScope(
    private val scope: Scope
) : Builder<List<Goal>>, LogicProgrammingScope by LogicProgrammingScope.empty() {

    private val goals = mutableListOf<Goal>()

    fun achieve(goal: Struct) {
        goals += Achieve.of(goal)
    }

    fun test(goal: Struct) {
        goals += Test.of(Belief.from(goal))
    }

    fun spawn(goal: Struct) {
        goals += Spawn.of(goal)
    }

    fun add(belief: Struct) {
        goals += AddBelief.of(Belief.from(belief))
    }

    fun remove(belief: Struct) {
        goals += RemoveBelief.of(Belief.from(belief))
    }

    fun update(belief: Struct) {
        goals += UpdateBelief.of(Belief.from(belief))
    }

    fun act(struct: Struct) {
        goals += Act.of(struct)
    }

    fun iact(struct: Struct) {
        goals += ActInternally.of(struct)
    }

    override fun build(): List<Goal> = goals.toList()
}
