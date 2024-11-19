package it.unibo.jakta.actions.impl

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ActionRequest
import it.unibo.jakta.actions.ActionResponse
import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction<C : ActionResult, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> (
    override val signature: Signature,
) : ASAction<C, Res, Req> {

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<C> = mutableListOf()

    final override suspend fun execute(argument: Req): Res {
        if (argument.arguments.size > signature.arity) {
            throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
        }
        action(argument)
        val res = argument.reply(result, effects.toMutableList())
        effects.clear()
        return res
    }

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    abstract fun action(request: Req)
}
