package it.unibo.jakta.actions

import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.tuprolog.core.Substitution

interface ASAction : Action {
    fun applySubstitution(substitution: Substitution): ASAction
    fun run(request: ActionRequest): ActionResponse
    fun addResults(substitution: Substitution)
}
