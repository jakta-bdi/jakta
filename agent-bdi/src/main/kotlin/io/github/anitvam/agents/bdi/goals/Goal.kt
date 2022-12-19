package io.github.anitvam.agents.bdi.goals

import io.github.anitvam.agents.bdi.beliefs.Belief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution

sealed interface Goal {
    val value: Struct

    fun applySubstitution(substitution: Substitution): Goal
}

sealed interface BeliefGoal : Goal {
    val belief: Struct
        get() = value
}

data class AddBelief(private val addedBelief: Belief) : BeliefGoal {
    override val value: Struct
        get() = addedBelief.rule.head
    override fun applySubstitution(substitution: Substitution) =
        AddBelief(addedBelief.applySubstitution(substitution))

    override fun toString(): String = "AddBelief(value=$addedBelief)"
}

data class RemoveBelief(private val removedBelief: Belief) : BeliefGoal {
    override val value: Struct
        get() = removedBelief.rule.head
    override fun applySubstitution(substitution: Substitution) =
        RemoveBelief(removedBelief.applySubstitution(substitution))

    override fun toString(): String = "RemoveBelief(value=$removedBelief)"
}

data class UpdateBelief(private val updatedBelief: Belief) : BeliefGoal {
    override val value: Struct
        get() = updatedBelief.rule.head
    override fun applySubstitution(substitution: Substitution) =
        UpdateBelief(updatedBelief.applySubstitution(substitution))

    override fun toString(): String = "UpdateBelief(value=$updatedBelief)"
}

data class Achieve(override val value: Struct) : Goal {
    override fun applySubstitution(substitution: Substitution) =
        Achieve(value.apply(substitution).castToStruct())
}

data class Test(val belief: Belief) : Goal {
    override val value: Struct
        get() = belief.rule.head
    override fun applySubstitution(substitution: Substitution) =
        Test(belief.applySubstitution(substitution))
}

data class Spawn(override val value: Struct) : Goal {
    override fun applySubstitution(substitution: Substitution) =
        Spawn(value.apply(substitution).castToStruct())
}

sealed interface ActionGoal : Goal {
    val action: Struct
        get() = value
}

data class Act(override val value: Struct) : ActionGoal {
    override fun applySubstitution(substitution: Substitution) =
        Act(value.apply(substitution).castToStruct())
}

data class ActInternally(override val value: Struct) : ActionGoal {
    override fun applySubstitution(substitution: Substitution) =
        ActInternally(value.apply(substitution).castToStruct())
}
