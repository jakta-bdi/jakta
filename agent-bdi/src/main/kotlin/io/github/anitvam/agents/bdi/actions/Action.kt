package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.solve.Signature

interface Action<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
}
