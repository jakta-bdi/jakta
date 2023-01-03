package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

sealed interface Action<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
}

abstract class AbstractAction<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> (
    override val signature: Signature,
) : Action<C, Res, Req> {

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<C> = mutableListOf()

    override fun execute(request: Req): Res {
        request.action()
        return request.reply(result, effects)
    }

    abstract fun Req.action()
}
