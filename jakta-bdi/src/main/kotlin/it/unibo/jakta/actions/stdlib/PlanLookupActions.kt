package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.AbstractAction
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.TestGoalFailure
import it.unibo.jakta.events.TestGoalInvocation
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

abstract class AbstractPlanLookupActions(
    name: String,
    var struct: Struct,
): AbstractAction(name, 1) {
    override fun applySubstitution(substitution: Substitution) {
        struct = struct.apply(substitution).castToStruct()
    }
}

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger.
 */
class Achieve(
    val planTrigger: Struct,
) : AbstractPlanLookupActions("Achieve", planTrigger) {
    override fun postExec(intention: ASIntention) {
        effects.add(EventChange.EventAddition(AchievementGoalInvocation(planTrigger, intention)))
        effects.add(IntentionChange.IntentionRemoval(intention)) // It gets added back in the queue after plan execution
    }

    override suspend fun action(request: ActionRequest) = Unit
}

/**
 * [Task.PlanExecution] which looks for a Plan with [TestGoalInvocation] trigger.
 */
class Test(
    val planTrigger: Struct,
) : AbstractPlanLookupActions("Test", planTrigger) {

    lateinit var solution: Solution
    override fun postExec(intention: ASIntention) {
        effects.add(EventChange.EventAddition(
            AchievementGoalInvocation(planTrigger, intention))
        )
        if (solution.isYes) {
            intention.pop()
            intention.applySubstitution(solution.substitution)
        } else {
            effects.add(EventChange.EventAddition(TestGoalFailure(planTrigger, intention)))
        }
    }

    override suspend fun action(request: ActionRequest) {
        solution = request.agentContext.beliefBase.getSolutionOf(planTrigger)
    }
}

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger
 * and executes it in another intention.
 */
class Spawn(
    val planTrigger: Struct,
) : AbstractPlanLookupActions("Spawn", planTrigger) {

    override fun postExec(intention: ASIntention) {
        effects.add(EventChange.EventAddition(AchievementGoalInvocation(planTrigger)))
        intention.pop()
    }

    override suspend fun action(request: ActionRequest) = Unit
}
