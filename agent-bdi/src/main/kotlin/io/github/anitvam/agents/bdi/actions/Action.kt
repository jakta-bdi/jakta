package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface Action<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
}

abstract class AbstractAction<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> (
    override val signature: Signature,
) : Action<C, Res, Req> {

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<C> = mutableListOf()

    final override fun execute(request: Req): Res {
        if (request.arguments.size > signature.arity) {
            throw IllegalArgumentException("ERROR: Wrong number of arguments for action ${signature.name}")
        }
        request.action()
        val res = request.reply(result, effects.toMutableList())
        effects.clear()
        return res
    }

    abstract fun Req.action()
}
