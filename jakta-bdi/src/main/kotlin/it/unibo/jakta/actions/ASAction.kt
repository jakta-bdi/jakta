package it.unibo.jakta.actions

import it.unibo.jakta.actions.effects.ActionSideEffect
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Signature

interface ASAction<Response, Request> : Action<Struct, ASBelief, Request, Response> where
    Request: ActionRequest<Response>,
    Response: ActionResponse
{
    val signature: Signature

    fun applySubstitution(substitution: Substitution)

    fun addResults(substitution: Substitution)
}
