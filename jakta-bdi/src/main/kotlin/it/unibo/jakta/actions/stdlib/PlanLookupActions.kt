package it.unibo.jakta.actions.stdlib

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ActionInvocationContext
import it.unibo.jakta.actions.SideEffect
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
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        Achieve(planTrigger.apply(substitution).castToStruct())

    override fun invoke(context: ASActionContext) = (context.agentContext as? ASAgent.ASAgentContext)
        ?.intentions?.nextIntention()?.let {
            listOf(
                EventChange.EventAddition(AchievementGoalInvocation(planTrigger, it.pop())),
                IntentionChange.IntentionRemoval(it), // It gets added back in the queue after plan execution
            )
        } ?: error("The action context is not an instance of Jakta AgentSpeak incarnation")
}

/**
 * [Task.PlanExecution] which looks for a Plan with [TestGoalInvocation] trigger.
 */
data class Test(
    val planTrigger: Struct,
) : AbstractExecutionAction() {

    private var solution: Solution = Solution.no(planTrigger)

    override fun applySubstitution(substitution: Substitution): ASAction =
        Test(planTrigger.apply(substitution).castToStruct())

    override fun invoke(context: ASActionContext): List<SideEffect> {
        solution = context.agentContext.beliefBase.select(planTrigger)
        return listOf(
            EventChange.EventAddition(
                AchievementGoalInvocation(
                    planTrigger,
                    (context.agentContext as? ASAgent.ASAgentContext)?.intentions?.nextIntention()
                        ?: error("The action context is not an instance of Jakta AgentSpeak incarnation"),
                ),
            ),
        )
    }
}

/**
 * [Task.PlanExecution] which looks for a Plan with [AchievementGoalInvocation] trigger
 * and executes it in another intention.
 */
data class Spawn(
    val planTrigger: Struct,
) : AbstractExecutionAction() {
    override fun applySubstitution(substitution: Substitution): ASAction =
        Spawn(planTrigger.apply(substitution).castToStruct())

    override fun invoke(context: ASActionContext) = listOf(
        EventChange.EventAddition(AchievementGoalInvocation(planTrigger)),
    )
}
