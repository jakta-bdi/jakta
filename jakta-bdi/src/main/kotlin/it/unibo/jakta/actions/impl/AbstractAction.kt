package it.unibo.jakta.actions.impl

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ActionRequest
import it.unibo.jakta.actions.ActionResponse
import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.executionstrategies.ExecutionResult
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

abstract class AbstractAction<C : ActionResult, Res : ActionResponse<C>, Req : ActionRequest<C, Res>> (
    override val signature: Signature,
) : ASAction<C, Res, Req> {

    protected var result: Substitution = Substitution.empty()

    protected val effects: MutableList<C> = mutableListOf()

    override fun addResults(substitution: Substitution) {
        result = substitution
    }

    abstract suspend fun action(request: Req)
}
