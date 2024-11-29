package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionResult
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature
import it.unibo.tuprolog.solve.sideffects.SideEffect

interface ASAction<in Context, out SideEffect, Response, Request> : Action<Struct, ASBelief, Request, Response> where
    SideEffect: ActionResult<Context>,
    Request: ActionRequest<Context, SideEffect, Response>,
    Response: ActionResponse<Context, SideEffect>
{
    val signature: Signature

    fun applySubstitution(substitution: Substitution)

    fun addResults(substitution: Substitution)
}
