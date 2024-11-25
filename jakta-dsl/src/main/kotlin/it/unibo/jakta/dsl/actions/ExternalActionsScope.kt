package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.actions.responses.ExternalResponse
import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.AbstractExternalAction

class ExternalActionsScope :
    ActionsScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction, ExternalActionScope>() {
    public override fun newAction(name: String, arity: Int, f: ExternalActionScope.() -> Unit): ExternalAction =
        object : AbstractExternalAction(name, arity) {
            override suspend fun action(request: ExternalRequest) {
                ExternalActionScope(this, request).f()
            }
        }
}
