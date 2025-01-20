package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.effects.EventChange
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.context.ASAgentContext
import it.unibo.jakta.context.AgentContext
import it.unibo.jakta.events.AchievementGoalFailure
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction (
    override val signature: Signature,
) : ASAction {

    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected var result: Substitution = Substitution.empty()
    protected val effects: MutableList<ActionSideEffect> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    protected fun fail() {
        result = Substitution.failed()
    }

    abstract fun postExec(intention: ASIntention)

    final override suspend fun execute(argument: ActionRequest): ActionResponse {

        val intention = argument.agentContext.intentions.nextIntention()

        // STATIC CHECKING
//        if (argument.arguments.size > signature.arity) {
//            val failure = AchievementGoalFailure(intention.currentPlan().trigger.value, intention)
//            val failureEvent = EventChange.EventAddition(failure)
//            // throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
//            effects.add(failureEvent)
//        }

        action(argument)
        postExec(intention)
        val response = argument.reply(result, effects.toMutableList())
        effects.clear()
        return response
    }

    abstract suspend fun action(request: ActionRequest)
}
