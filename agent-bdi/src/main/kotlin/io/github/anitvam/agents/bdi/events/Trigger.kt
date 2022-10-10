package io.github.anitvam.agents.bdi.events

import io.github.anitvam.agents.bdi.beliefs.Belief
import io.github.anitvam.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.core.Struct
import io.github.anitvam.agents.bdi.goals.Test
import io.github.anitvam.agents.bdi.goals.Achieve

/** [Trigger] denotes the change that took place for the [Event] generation */
sealed interface Trigger {

    val value: Struct

    companion object {

        /** Static Factory for [BeliefBaseAddition] trigger */
        fun ofBeliefBaseAddition(belief: Belief) = BeliefBaseAddition(belief)

        /** Static Factory for [BeliefBaseRemoval] trigger */
        fun ofBeliefBaseRemoval(belief: Belief) = BeliefBaseRemoval(belief)
    }
}

/** [Trigger] generated after a [Belief] addition (or removal) from the [BeliefBase] */
sealed interface BeliefBaseRevision: Trigger {

    /** The head of the [Belief] that is inserted (or removed) from the [BeliefBase] */
    val belief: Struct
        get() = value
}

/** [BeliefBaseRevision] generated after a [Belief] addition to agent's [BeliefBase] */
class BeliefBaseAddition(override val value: Struct) : BeliefBaseRevision

/** [BeliefBaseRevision] generated after a [Belief] removal from agent's [BeliefBase] */
class BeliefBaseRemoval(override val value: Struct) : BeliefBaseRevision

/** [Trigger] of an event made by a [Test] Goal */
sealed interface TestGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

/** [TestGoalTrigger] generated after an invocation of a [Test] Goal */
class TestGoalInvocation(override val value: Struct) : TestGoalTrigger

/** [TestGoalTrigger] generated after a failure of a [Test] Goal */
class TestGoalFailure(override val value: Struct) : TestGoalTrigger

/** [Trigger] of an event made by a [Achieve] Goal */
sealed interface AchievementGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

/** [AchievementGoalTrigger] generated after the invocation of a [Achieve] Goal */
class AchievementGoalInvocation(override val value: Struct) : AchievementGoalTrigger


/** [AchievementGoalTrigger] generated after the failure of a [Achieve] Goal*/
class AchievementGoalFailure(override val value: Struct) : AchievementGoalTrigger
