package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

abstract class AbstractAction : ASAction {
    protected var result: Substitution = Substitution.empty()
    private val effects: MutableList<SideEffect> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    protected fun fail() {
        result = Substitution.failed()
    }

    override fun runAction(request: ActionRequest): ActionResponse {
        // val request = p1 as? ActionRequest ?: error("Not executing with a valid action request")
        val intention = request.agentContext.intentions.nextIntention()
        val effects = invoke(request)
        postExec(intention)
        val response = request.reply(result, effects.toMutableList() + this.effects)
        // effects.clear()
        return response
    }

    private fun postExec(intention: ASIntention) {
        var newIntention = intention.pop()
        if (result.isSuccess) {
            if (newIntention.stack.isNotEmpty()) {
                newIntention = newIntention.applySubstitution(result)
            }
            effects.add(IntentionChange.Update(newIntention))
        } else {
            val trigger = intention.currentPlan().trigger
            val failure = AchievementGoalFailure(trigger, intention)
            val failureEvent = EventChange.Addition(failure)
            effects.add(failureEvent) // Add Failure Event to be handled in future lifecycle steps
        }
    }

    abstract class WithoutSideEffects :
        AbstractAction(),
        Action.WithoutSideEffect<ASBelief, Struct, Solution> {
        final override fun invoke(context: ActionInvocationContext<ASBelief, Struct, Solution>): List<SideEffect> =
            super.invoke(context)
    }
}
