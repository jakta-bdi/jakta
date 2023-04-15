package it.unibo.jakta.agents.bdi.events

import it.unibo.jakta.agents.bdi.beliefs.Belief
import it.unibo.jakta.agents.bdi.beliefs.BeliefBase
import it.unibo.tuprolog.core.Struct
import it.unibo.jakta.agents.bdi.goals.Test
import it.unibo.jakta.agents.bdi.goals.Achieve

/** [Trigger] denotes the change that took place for the [Event] generation. */
interface Trigger {
    val value: Struct
}

/** [Trigger] generated after a [Belief] addition (or removal) from the [BeliefBase]. */
interface BeliefBaseRevision : Trigger {

    /** The head of the [Belief] that is inserted (or removed) from the [BeliefBase]. */
    val belief: Struct
        get() = value
}

/** [BeliefBaseRevision] generated after a [Belief] addition to agent's [BeliefBase]. */
class BeliefBaseAddition(private val addedBelief: Belief) : BeliefBaseRevision {
    override val value: Struct
        get() = addedBelief.rule.head

    override fun toString(): String = "BeliefBaseAddition(value=$value)"
}

/** [BeliefBaseRevision] generated after a [Belief] removal from agent's [BeliefBase]. */
data class BeliefBaseRemoval(private val removedBelief: Belief) : BeliefBaseRevision {
    override val value: Struct
        get() = removedBelief.rule.head
    override fun toString(): String = "BeliefBaseRemoval(value=$value)"
}

data class BeliefBaseUpdate(private val removedBelief: Belief) : BeliefBaseRevision {
    override val value: Struct
        get() = removedBelief.rule.head
    override fun toString(): String = "BeliefBaseUpdate(value=$value)"
}

/** [Trigger] of an event made by a [Test] Goal. */
interface TestGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

/** [TestGoalTrigger] generated after an invocation of a [Test] Goal. */
data class TestGoalInvocation(override val value: Struct) : TestGoalTrigger

/** [TestGoalTrigger] generated after a failure of a [Test] Goal. */
data class TestGoalFailure(override val value: Struct) : TestGoalTrigger

/** [Trigger] of an event made by a [Achieve] Goal. */
interface AchievementGoalTrigger : Trigger {
    val goal: Struct
        get() = value
}

/** [AchievementGoalTrigger] generated after the invocation of a [Achieve] Goal. */
data class AchievementGoalInvocation(override val value: Struct) : AchievementGoalTrigger

/** [AchievementGoalTrigger] generated after the failure of a [Achieve] Goal. */
data class AchievementGoalFailure(override val value: Struct) : AchievementGoalTrigger
