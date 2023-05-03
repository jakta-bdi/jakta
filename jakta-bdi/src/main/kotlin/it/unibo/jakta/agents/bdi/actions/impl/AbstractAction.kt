package it.unibo.jakta.agents.bdi.actions.impl

import it.unibo.jakta.agents.bdi.actions.Action
import it.unibo.jakta.agents.bdi.actions.ActionRequest
import it.unibo.jakta.agents.bdi.actions.ActionResponse
import it.unibo.jakta.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> (
    override val signature: Signature,
) : Action<C, Res, Req> {

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<C> = mutableListOf()

    final override fun execute(request: Req): Res {
        if (request.arguments.size > signature.arity) {
            throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
        }
        action(request)
        val res = request.reply(result, effects.toMutableList())
        effects.clear()
        return res
    }

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    abstract fun action(request: Req)
}
