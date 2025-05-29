package it.unibo.jakta.actions

import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.beliefs.ASBelief
import it.unibo.tuprolog.core.Struct
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.solve.Solution

interface ASAction : Action<ASBelief, Struct, Solution> {
    fun applySubstitution(substitution: Substitution): ASAction

    fun runAction(request: ActionRequest): ActionResponse

    fun addResults(substitution: Substitution)
}
