package it.unibo.jakta.events

import it.unibo.jakta.beliefs.PrologBelief
import it.unibo.jakta.events.impl.EventImpl
import it.unibo.jakta.goals.Achieve
import it.unibo.jakta.goals.Test
import it.unibo.jakta.intentions.Intention
import it.unibo.jakta.plans.Test
import it.unibo.tuprolog.core.Struct

interface PrologEvent : Event<Trigger> {

    companion object {
        /**
         * Generates a new [Event]
         * @param trigger: the [Trigger] of the [Event]
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun of(trigger: Trigger, intention: Intention? = null): PrologEvent =
            EventImpl(trigger, intention)

        /**
         * Generates an [Event] with a [BeliefBaseAddition] trigger.
         * @param belief: the belief that triggered this Event
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun ofBeliefBaseAddition(belief: PrologBelief, intention: Intention? = null) =
            of(BeliefBaseAddition(belief), intention)

        /**
         * Generates an [Event] with a [BeliefBaseRemoval] trigger.
         * @param belief: the belief that triggered this Event
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun ofBeliefBaseRemoval(belief: PrologBelief, intention: Intention? = null) =
            of(BeliefBaseRemoval(belief), intention)

        fun ofBeliefBaseUpdate(belief: PrologBelief, intention: Intention? = null) =
            of(BeliefBaseUpdate(belief), intention)

        /**
         * Generates an [Event] with a [TestGoalInvocation] trigger.
         * @param testGoal: the [Test] PrologGoal that triggered this Event
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun ofTestGoalInvocation(testGoal: Test, intention: Intention? = null) =
            of(TestGoalInvocation(testGoal.), intention)

        /**
         * Generates an [Event] with a [TestGoalFailure] trigger.
         * @param testGoal: the PrologGoal that triggered this Event
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun ofTestGoalFailure(testGoal: Struct, intention: Intention? = null) =
            of(TestGoalFailure(testGoal), intention)

        /**
         * Generates an [Event] with a [AchievementGoalInvocation] trigger.
         * @param achievementGoal: the [Achieve] PrologGoal that triggered this Event
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun ofAchievementGoalInvocation(
            achievementGoal: Achieve,
            intention: Intention? = null,
        ) = of(AchievementGoalInvocation(achievementGoal.value), intention)

        /**
         * Generates an [Event] with a [AchievementGoalFailure] trigger.
         * @param achievementGoal: the PrologGoal that triggered this Event
         * @param intention: if the event is internal, this parameter specifies the intention id where the event belongs.
         * If the event is external, this value is set to null. It's default value is null.
         * @return a new instance of [Event]
         */
        fun ofAchievementGoalFailure(achievementGoal: Struct, intention: Intention? = null) =
            of(AchievementGoalFailure(achievementGoal), intention)
    }
}
