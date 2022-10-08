package io.github.anitvam.agents.bdi.events

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.core.Struct
import io.github.anitvam.agents.bdi.events.BeliefBaseRevision.BeliefBaseRemoval
import io.github.anitvam.agents.bdi.events.BeliefBaseRevision.BeliefBaseAddition
import it.unibo.tuprolog.core.Clause

/** [Trigger] denotes the change that took place for the [Event] generation */
sealed interface Trigger {

    val value: Clause

    companion object {

        /** Static Factory for [BeliefBaseAddition] trigger */
        fun ofBeliefBaseAddition(belief: Belief) = BeliefBaseAddition(belief)

        /** Static Factory for [BeliefBaseRemoval] trigger */
        fun ofBeliefBaseRemoval(belief: Belief) = BeliefBaseRemoval(belief)
    }
}

/** [BeliefBaseRevision] is a [Trigger] that took place after a [Belief] addition (or removal) from the [BeliefBase] */
sealed interface BeliefBaseRevision: Trigger {

    /** The [Belief] that is inserted (or removed) from the [BeliefBase] */
    val belief: Belief
        get() = value

    /** [BeliefBaseRevision] generated after a [Belief] addition to agent's [BeliefBase] */
    class BeliefBaseAddition(override val value: Belief) : BeliefBaseRevision

    /** [BeliefBaseRevision] generated after a [Belief] removal from agent's [BeliefBase] */
    class BeliefBaseRemoval(override val value: Belief) : BeliefBaseRevision
}

sealed interface TestGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

sealed interface TestGoalInvocation : TestGoalTrigger

sealed interface TestGoalFailure : TestGoalTrigger

sealed interface AchievementGoalTrigger : Trigger {
    val goal: Struct
        get() = goal
}

sealed interface AchievementGoalInvocation : AchievementGoalTrigger

sealed interface AchievementGoalFailure : AchievementGoalTrigger
