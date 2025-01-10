package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractInternalAction
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.TestGoalInvocation
import it.unibo.jakta.intentions.IntentionPool
import it.unibo.jakta.plans.Task
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractPlanExecutionTask(
    name: String,
    var struct: Struct,
): AbstractInternalAction(Signature(name, 1)) {
    override fun applySubstitution(substitution: Substitution) {
        struct = struct.apply(substitution).castToStruct()
    }
}


/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger.
 */
class Achieve(
    val planTrigger: Struct,
) : AbstractPlanExecutionTask("Achieve", planTrigger) {
    override suspend fun action(request: InternalRequest) { }
}

/**
 * [Task.PlanExecution] which looks for a Plan with [TestGoalInvocation] trigger.
 */
data class Test(
    override val event: TestGoalInvocation,
) : AbstractPlanExecutionTask("Test", )

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger
 * and executes it in another intention.
 */
data class Spawn(
    override val event: Event,
) : Task.PlanExecution
