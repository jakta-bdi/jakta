package io.github.anitvam.agents.bdi.goals

import java.sql.Struct

sealed interface Goal {
    val value: Struct
}

sealed interface BeliefGoal : Goal {
    val belief: Struct
        get() = value
}

sealed interface AddBelief : BeliefGoal

sealed interface RemoveBelief : BeliefGoal

sealed interface UpdateBelief : BeliefGoal


sealed interface Achieve : Goal

sealed interface Test : Goal

sealed interface Spawn : Goal

sealed interface ActionGoal : Goal {
    val action: Struct
        get() = value
}

sealed interface Act : ActionGoal

sealed interface ActInternally : ActionGoal
