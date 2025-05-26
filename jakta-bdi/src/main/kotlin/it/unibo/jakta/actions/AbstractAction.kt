package it.unibo.jakta.actions

import it.unibo.jakta.ASAgent
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.intentions.ASIntention
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction(
    override val signature: Signature,
) : ASAction {
    constructor(name: String, arity: Int) : this(Signature(name, arity))

    protected var result: Substitution = Substitution.empty()
    protected val effects: MutableList<SideEffect> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    protected fun fail() {
        result = Substitution.failed()
    }

    abstract fun postExec(intention: ASIntention)

    suspend fun execute(argument: ActionRequest): ActionResponse {
        val intention = (argument.agentContext as ASAgent.ASAgentContext).intentions.nextIntention()

        // STATIC CHECKING
//        if (argument.arguments.size > signature.arity) {
//            val failure = AchievementGoalFailure(intention.currentPlan().trigger.value, intention)
//            val failureEvent = EventChange.EventAddition(failure)
//            // throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
//            effects.add(failureEvent)
//        }

        val effects = invoke(argument)
        postExec(intention)
        val response = argument.reply(result, effects.toMutableList() + this.effects)
        // effects.clear()
        return response
    }
}
