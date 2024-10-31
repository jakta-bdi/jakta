package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface ASAction<C : ActionResult, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> {
    val signature: Signature
    fun execute(request: Req): Res
    fun addResults(substitution: Substitution)
}
