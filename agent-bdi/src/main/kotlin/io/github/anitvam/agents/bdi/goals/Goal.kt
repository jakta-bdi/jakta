package io.github.anitvam.agents.bdi.goals

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.goals.impl.AchieveImpl
import io.github.anitvam.agents.bdi.goals.impl.ActInternallyImpl
import io.github.anitvam.agents.bdi.goals.impl.ActImpl
import io.github.anitvam.agents.bdi.goals.impl.SpawnImpl
import io.github.anitvam.agents.bdi.goals.impl.TestImpl
import io.github.anitvam.agents.bdi.goals.impl.AddBeliefImpl
import io.github.anitvam.agents.bdi.goals.impl.RemoveBeliefImpl
import io.github.anitvam.agents.bdi.goals.impl.UpdateBeliefImpl
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Truth

sealed interface Goal {
    val value: Struct

    fun applySubstitution(substitution: Substitution): Goal

    fun copy(value: Struct = this.value): Goal
}

class EmptyGoal(override val value: Struct = Truth.TRUE) : Goal {
    override fun applySubstitution(substitution: Substitution): Goal = this

    override fun copy(value: Struct): Goal = EmptyGoal(value)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmptyGoal

        if (value != other.value) return false

        return true
    }

    override fun hashCode() = value.hashCode()
}

sealed interface BeliefGoal : Goal {
    val belief: Struct
        get() = value
}

interface AddBelief : BeliefGoal {
    companion object {
        fun of(belief: Belief): AddBelief = AddBeliefImpl(belief)
    }
}

interface RemoveBelief : BeliefGoal {
    companion object {
        fun of(belief: Belief): RemoveBelief = RemoveBeliefImpl(belief)
    }
}

interface UpdateBelief : BeliefGoal {
    companion object {
        fun of(belief: Belief): UpdateBelief = UpdateBeliefImpl(belief)
    }
}

interface Achieve : Goal {
    companion object {
        fun of(value: Struct): Achieve = AchieveImpl(value)
    }
}

interface Test : Goal {
    companion object {
        fun of(belief: Belief): Test = TestImpl(belief)
    }
}

interface Spawn : Goal {
    companion object {
        fun of(value: Struct): Spawn = SpawnImpl(value)
    }
}

sealed interface ActionGoal : Goal {
    val action: Struct
        get() = value
}

interface Act : ActionGoal {
    companion object {
        fun of(value: Struct): Act = ActImpl(value)
    }
}

interface ActInternally : ActionGoal {
    companion object {
        fun of(value: Struct): ActInternally = ActInternallyImpl(value)
    }
}
