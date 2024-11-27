package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface ASAction<out C, Res, Req> : Action<Struct, ASBelief, Req, Res> where
    Req: ActionRequest<C, Res>,
    Res: ActionResponse<C>,
    C: ActionResult
{
    val signature: Signature

    fun applySubstitution(substitution: Substitution)

    fun addResults(substitution: Substitution)
}
