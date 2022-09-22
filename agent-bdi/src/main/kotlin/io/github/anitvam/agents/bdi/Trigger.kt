package io.github.anitvam.agents.bdi

import java.sql.Struct

sealed interface Trigger {
    val value: Struct
}

sealed interface BeliefBaseRevision : Trigger {
    val belief: Struct
        get() = value
}

sealed interface BeliefBaseAddition : BeliefBaseRevision

sealed interface BeliefBaseRemoval : BeliefBaseRevision

sealed interface TestGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

sealed interface TestGoalInvocation : TestGoalTrigger

sealed interface TestGoalFailure : TestGoalTrigger

sealed interface AchievementGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

sealed interface AchievementGoalInvocation : AchievementGoalTrigger

sealed interface AchievementGoalFailure : AchievementGoalTrigger
