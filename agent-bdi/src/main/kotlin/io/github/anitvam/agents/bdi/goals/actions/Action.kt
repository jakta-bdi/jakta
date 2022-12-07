package io.github.anitvam.agents.bdi.goals.actions

import io.github.anitvam.agents.bdi.goals.actions.effects.SideEffect
import it.unibo.tuprolog.solve.Signature

sealed interface Action<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
}
