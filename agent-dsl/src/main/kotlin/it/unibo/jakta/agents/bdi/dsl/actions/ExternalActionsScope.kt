package it.unibo.jakta.agents.bdi.dsl.actions

import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.ExternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange
import it.unibo.jakta.agents.bdi.actions.impl.AbstractExternalAction

class ExternalActionsScope :
    ActionsScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction, ExternalActionScope>() {
    public override fun newAction(name: String, arity: Int, f: ExternalActionScope.() -> Unit): ExternalAction =
        object : AbstractExternalAction(name, arity) {
            override fun action(request: ExternalRequest) {
                ExternalActionScope(this, request).f()
            }
        }
}
