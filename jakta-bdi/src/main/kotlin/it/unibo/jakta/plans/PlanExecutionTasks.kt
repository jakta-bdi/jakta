package it.unibo.jakta.plans

import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.Event
import it.unibo.jakta.events.TestGoalInvocation

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger.
 */
data class Achieve(
    override val event: Event<AchievementGoalInvocation>,
) : Task.PlanExecution<AchievementGoalInvocation>

/**
 * [Task.PlanExecution] which looks for a Plan with [TestGoalInvocation] trigger.
 */
data class Test(
    override val event: Event<TestGoalInvocation>,
) : Task.PlanExecution<TestGoalInvocation>

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger
 * and executes it in another intention.
 */
data class Spawn(
    override val event: Event,
) : Task.PlanExecution
