package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.actions.effects.BeliefChange
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.effects.Pause
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction<C : ActionResult, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> (
    override val signature: Signature,
) : ASAction<C, Res, Req> {

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<ActionResult> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    final override suspend fun execute(argument: Req): Res {
        val intention = argument.agentContext.intentions.nextIntention()

        if (argument.arguments.size > signature.arity) {
            // throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
            effects.add(
                EventChange.EventAddition(
                    AchievementGoalFailure(intention.currentPlan().trigger.trigger, intention)
                )
            )
        }

        action(argument)

        val res = argument.reply(result, effects.toMutableList())

        effects.clear()

        if (res.substitution.isSuccess) {
            if (intention.recordStack.isNotEmpty()) {
                intention.applySubstitution(res.substitution)
            }
        } else {
            effects.add(
                EventChange.EventAddition(
                    AchievementGoalFailure(intention.currentPlan().trigger.trigger, intention)
                )
            )
        }
        return res
    }

    abstract suspend fun action(request: Req)
}
