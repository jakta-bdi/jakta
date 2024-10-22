package it.unibo.jakta.events

import it.unibo.jakta.beliefs.BeliefBase

/** [Trigger] denotes the change that took place for the [Event] generation. */
sealed interface Trigger<out Query> {
    val value: Query
}

sealed interface BeliefBaseRevision<out Query> : Trigger<Query> {
    /** [Trigger] generated after a Belief addition to agent's [BeliefBase]. */
    data class BeliefBaseAddition<out Query>(override val value: Query) : BeliefBaseRevision<Query> {
        override fun toString(): String = "BeliefBaseAddition(value=$value)"
    }

    /** [Trigger] generated after a Belief removal from agent's [BeliefBase]. */
    data class BeliefBaseRemoval<out Query>(override val value: Query) : BeliefBaseRevision<Query> {
        override fun toString(): String = "BeliefBaseRemoval(value=$value)"
    }

    data class BeliefBaseUpdate<out Query>(override val value: Query) : BeliefBaseRevision<Query> {
        override fun toString(): String = "BeliefBaseUpdate(value=$value)"
    }
}

/** [Trigger] of an event made by a [Test] Goal. */
sealed interface TestGoalTrigger<out Query> : Trigger<Query> {
    /** [Trigger] generated after an invocation of a [Test] Goal. */
    data class TestGoalInvocation<out Query>(override val value: Query) : TestGoalTrigger<Query> {
        override fun toString(): String = "TestGoalInvocation(value=$value)"
    }

    /** [Trigger] generated after a failure of a [Test] Goal. */
    data class TestGoalFailure<out Query>(override val value: Query) : TestGoalTrigger<Query> {
        override fun toString(): String = "TestGoalFailure(value=$value)"
    }
}

/** [Trigger] of an event made by a [Achieve] Goal. */
sealed interface AchievementGoalTrigger<out Query> : Trigger<Query> {

    /** [Trigger] generated after the invocation of a [Achieve] Goal. */
    data class AchievementGoalInvocation<out Query>(override val value: Query) : AchievementGoalTrigger<Query> {
        override fun toString(): String = "AchievementGoalInvocation(value=$value)"
    }

    /** [Trigger] generated after the failure of a [Achieve] Goal. */
    data class AchievementGoalFailure<out Query>(override val value: Query) : AchievementGoalTrigger<Query> {
        override fun toString(): String = "AchievementGoalFailure(value=$value)"
    }
}
