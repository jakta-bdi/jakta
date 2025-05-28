package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.requests.ASActionContext
import it.unibo.jakta.beliefs.ASBeliefBase
import it.unibo.jakta.beliefs.impl.ASBeliefBaseImpl.Companion.operatorExtension
import it.unibo.jakta.events.AchievementGoalInvocation
import it.unibo.jakta.events.TestGoalFailure
import it.unibo.jakta.events.TestGoalInvocation
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution
import it.unibo.tuprolog.solve.Solver
import it.unibo.tuprolog.solve.flags.TrackVariables
import it.unibo.tuprolog.solve.flags.TrackVariables.ON
import it.unibo.tuprolog.solve.flags.Unknown
import it.unibo.tuprolog.theory.Theory

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger.
 */
data class Achieve(
    val planTrigger: Struct,
) : AbstractExecutionAction.WithoutSideEffects() {
    override fun postExec(intention: ASIntention) {
        effects.add(EventChange.EventAddition(AchievementGoalInvocation(planTrigger, intention)))
        effects.add(IntentionChange.IntentionRemoval(intention)) // It gets added back in the queue after plan execution
    }

    override fun applySubstitution(substitution: Substitution): ASAction =
        Achieve(planTrigger.apply(substitution).castToStruct())

    override fun execute(context: ASActionContext) = Unit
}

/**
 * [Task.PlanExecution] which looks for a Plan with [TestGoalInvocation] trigger.
 */
data class Test(
    val planTrigger: Struct,
) : AbstractExecutionAction.WithoutSideEffects() {

    private var solution: Solution = Solution.no(planTrigger)

    override fun postExec(intention: ASIntention) {
        effects.add(
            EventChange.EventAddition(
                AchievementGoalInvocation(planTrigger, intention),
            ),
        )
        if (solution.isYes) {
            intention.pop()
            intention.applySubstitution(solution.substitution)
        } else {
            effects.add(EventChange.EventAddition(TestGoalFailure(planTrigger, intention)))
        }
    }

    override fun applySubstitution(substitution: Substitution): ASAction =
        Test(planTrigger.apply(substitution).castToStruct())

    override fun execute(context: ASActionContext) {
        solution = context.agentContext.beliefBase.select(planTrigger)
    }
}

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger
 * and executes it in another intention.
 */
data class Spawn(
    val planTrigger: Struct,
) : AbstractExecutionAction.WithoutSideEffects() {

    override fun postExec(intention: ASIntention) {
        effects.add(EventChange.EventAddition(AchievementGoalInvocation(planTrigger)))
        intention.pop()
    }

    override fun applySubstitution(substitution: Substitution): ASAction =
        Spawn(planTrigger.apply(substitution).castToStruct())

    override fun execute(context: ASActionContext) = Unit
}
