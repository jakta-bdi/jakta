package io.github.anitvam.agents.bdi.goals

import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

sealed interface Goal {
    val value: Struct

    fun applySubstitution(substitution: Substitution) : Goal
}

sealed interface BeliefGoal : Goal {
    val belief: Struct
        get() = value
}

data class AddBelief(override val value: Struct) : BeliefGoal {
    override fun applySubstitution(substitution: Substitution) = AddBelief(value.apply(substitution).castToStruct())
}

data class RemoveBelief(override val value: Struct) : BeliefGoal {
    override fun applySubstitution(substitution: Substitution) = RemoveBelief(value.apply(substitution).castToStruct())
}

data class UpdateBelief(override val value: Struct) : BeliefGoal {
    override fun applySubstitution(substitution: Substitution) = UpdateBelief(value.apply(substitution).castToStruct())
}


data class Achieve(override val value: Struct) : Goal {
    override fun applySubstitution(substitution: Substitution) = Achieve(value.apply(substitution).castToStruct())
}

data class Test(override val value: Struct) : Goal {
    override fun applySubstitution(substitution: Substitution) = Test(value.apply(substitution).castToStruct())
}

data class Spawn(override val value: Struct) : Goal {
    override fun applySubstitution(substitution: Substitution) = Spawn(value.apply(substitution).castToStruct())
}

sealed interface ActionGoal : Goal {
    val action: Struct
        get() = value
}

data class Act(override val value: Struct) : ActionGoal {
    override fun applySubstitution(substitution: Substitution) = Act(value.apply(substitution).castToStruct())
}

data class ActInternally(override val value: Struct) : ActionGoal {
    override fun applySubstitution(substitution: Substitution) = ActInternally(value.apply(substitution).castToStruct())
}
