package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.context.ASMutableAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.sideffects.SideEffect

abstract class AbstractAction<Request: ActionRequest> (
    override val signature: Signature,
) : ASAction<Request> {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected var result: Substitution = Substitution.empty()
    protected val effects: MutableList<ActionSideEffect> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    final override suspend fun execute(argument: Request): ActionResponse {

//        val sideEffect = ActionSideEffect { context: ASMutableAgentContext ->
//            context.mutableEventList.add(eadfewfew)
//        }

        val intention = argument.agentContext.intentions.nextIntention()

        if (argument.arguments.size > signature.arity) {
            val failure = AchievementGoalFailure(intention.currentPlan().trigger.trigger, intention)
            val failureEvent = EventChange.EventAddition(failure)
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
            val failureEvent = EventChange.EventAddition(failure)
            effects.add(failureEvent)
        }
        return res
    }

    abstract suspend fun action(request: Request)
}
