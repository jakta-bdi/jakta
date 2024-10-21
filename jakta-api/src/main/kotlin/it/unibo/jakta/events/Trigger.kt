package it.unibo.jakta.events

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase

/** [Trigger] denotes the change that took place for the [Event] generation. */
sealed interface Trigger<out X> {
    val value: X
}

/** [Trigger] generated after a [Belief] addition (or removal) from the [BeliefBase]. */
interface BeliefBaseRevision<out X : Belief<*>> : Trigger<X>

/** [Trigger] generated after a [Belief] addition to agent's [BeliefBase]. */
class BeliefBaseAddition<X : Belief<*>>(override val value: X) : BeliefBaseRevision<X> {
    override fun toString(): String = "BeliefBaseAddition(value=$value)"
}

/** [Trigger] generated after a [Belief] removal from agent's [BeliefBase]. */
data class BeliefBaseRemoval<X : Belief<*>>(override val value: X) : BeliefBaseRevision<X> {
    override fun toString(): String = "BeliefBaseRemoval(value=$value)"
}

data class BeliefBaseUpdate<X : Belief<*>>(override val value: X) : BeliefBaseRevision<X> {
    override fun toString(): String = "BeliefBaseUpdate(value=$value)"
}

/** [Trigger] of an event made by a [Test] Goal. */
interface TestGoalTrigger<out X> : Trigger<X>

/** [Trigger] generated after an invocation of a [Test] Goal. */
data class TestGoalInvocation<X>(override val value: X) : TestGoalTrigger<X> {
    override fun toString(): String = "TestGoalInvocation(value=$value)"
}

/** [Trigger] generated after a failure of a [Test] Goal. */
data class TestGoalFailure<X>(override val value: X) : TestGoalTrigger<X> {
    override fun toString(): String = "TestGoalFailure(value=$value)"
}

/** [Trigger] of an event made by a [Achieve] Goal. */
interface AchievementGoalTrigger<out X> : Trigger<X>

/** [Trigger] generated after the invocation of a [Achieve] Goal. */
data class AchievementGoalInvocation<X>(override val value: X) : AchievementGoalTrigger<X> {
    override fun toString(): String = "AchievementGoalInvocation(value=$value)"
}

/** [Trigger] generated after the failure of a [Achieve] Goal. */
data class AchievementGoalFailure<X>(override val value: X) : AchievementGoalTrigger<X> {
    override fun toString(): String = "AchievementGoalFailure(value=$value)"
}
