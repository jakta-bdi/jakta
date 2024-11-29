package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction<Response, Request> (
    override val signature: Signature,
) : ASAction<Response, Request> where
    Request: ActionRequest<Response>,
    Response: ActionResponse
{

    protected var result: Substitution = Substitution.empty()
    protected abstract var mutableAgentContext: ASMutableAgentContext // TODO: somehow this must be provided
    protected val effects: MutableList<ActionSideEffect> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    final override suspend fun execute(argument: Request): Response {
        val intention = argument.agentContext.intentions.nextIntention()

        if (argument.arguments.size > signature.arity) {
            val failure = AchievementGoalFailure(intention.currentPlan().trigger.trigger, intention)
            val failureEvent = EventChange.EventAddition(failure, mutableAgentContext)
            // throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
            effects.add(failureEvent)
        }

        action(argument)

        val res = argument.reply(result, effects.toMutableList())

        effects.clear()

        if (res.substitution.isSuccess) {
            if (intention.recordStack.isNotEmpty()) {
                intention.applySubstitution(res.substitution)
            }
        } else {
            val trigger = intention.currentPlan().trigger.trigger // TODO: that trigger.trigger looks awful
            val failure = AchievementGoalFailure(trigger, intention)
            val failureEvent = EventChange.EventAddition(failure, mutableAgentContext)
            effects.add(failureEvent)
        }
        return res
    }

    abstract suspend fun action(request: Request)
}
