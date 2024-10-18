package it.unibo.jakta.events

import it.unibo.jakta.beliefs.Belief
import it.unibo.jakta.beliefs.BeliefBase

/** [Trigger] denotes the change that took place for the [Event] generation. */
sealed interface Trigger<X> {
    val value: X
}

/** [Trigger] generated after a [Belief] addition to agent's [BeliefBase]. */
class BeliefBaseAddition(override val value: Belief<*>) : Trigger<Belief<*>> {
    override fun toString(): String = "BeliefBaseAddition(value=$value)"
}

/** [Trigger] generated after a [Belief] removal from agent's [BeliefBase]. */
data class BeliefBaseRemoval(override val value: Belief<*>) : Trigger<Belief<*>> {
    override fun toString(): String = "BeliefBaseRemoval(value=$value)"
}

data class BeliefBaseUpdate(override val value: Belief<*>) : Trigger<Belief<*>> {
    override fun toString(): String = "BeliefBaseUpdate(value=$value)"
}

/** [Trigger] generated after an invocation of a [Test] Goal. */
data class TestGoalInvocation<X>(override val value: X) : Trigger<X> {
    override fun toString(): String = "TestGoalInvocation(value=$value)"
}

/** [Trigger] generated after a failure of a [Test] Goal. */
data class TestGoalFailure<X>(override val value: X) : Trigger<X> {
    override fun toString(): String = "TestGoalFailure(value=$value)"
}

/** [Trigger] generated after the invocation of a [Achieve] Goal. */
data class AchievementGoalInvocation<X>(override val value: X) : Trigger<X> {
    override fun toString(): String = "AchievementGoalInvocation(value=$value)"
}

/** [Trigger] generated after the failure of a [Achieve] Goal. */
data class AchievementGoalFailure<X>(override val value: X) : Trigger<X> {
    override fun toString(): String = "AchievementGoalFailure(value=$value)"
}
