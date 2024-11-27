package it.unibo.jakta.plans

import it.unibo.jakta.actions.AbstractInternalAction
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.TestGoalInvocation
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
    val plan: Struct,
    val event: Event<AchievementGoalInvocation>,
) : AbstractPlanExecutionTask("Achieve", plan)

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
