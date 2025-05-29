package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.IntentionChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution

abstract class AbstractAction : ASAction {

    protected var result: Substitution = Substitution.empty()
    private val effects: MutableList<SideEffect> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    protected fun fail() {
        result = Substitution.failed()
    }

    fun postExec(intention: ASIntention) {
        var newIntention = intention.pop()
        if (result.isSuccess) {
            if (newIntention.recordStack.isNotEmpty()) {
                newIntention = newIntention.applySubstitution(result)
            }
            effects.add(IntentionChange.IntentionUpdate(newIntention))
        } else {
            val trigger = intention.currentPlan().trigger.value
            val failure = AchievementGoalFailure(trigger, intention)
            val failureEvent = EventChange.EventAddition(failure)
            effects.add(failureEvent) // Add Failure Event to be handled in future lifecycle steps
        }
    }

    override fun run(request: ActionRequest): ActionResponse {
        val intention = request.agentContext.intentions.nextIntention()

        // STATIC CHECKING
//        if (argument.arguments.size > signature.arity) {
//            val failure = AchievementGoalFailure(intention.currentPlan().trigger.value, intention)
//            val failureEvent = EventChange.EventAddition(failure)
//            // throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
//            effects.add(failureEvent)
//        }

        val effects = invoke(request)
        postExec(intention)
        val response = request.reply(result, effects.toMutableList() + this.effects)
        // effects.clear()
        return response
    }
}
