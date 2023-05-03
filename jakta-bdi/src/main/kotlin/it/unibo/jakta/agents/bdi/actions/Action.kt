package it.unibo.jakta.agents.bdi.actions

import it.unibo.jakta.agents.bdi.actions.effects.SideEffect
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface Action<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
    fun addResults(substitution: Substitution)
}
