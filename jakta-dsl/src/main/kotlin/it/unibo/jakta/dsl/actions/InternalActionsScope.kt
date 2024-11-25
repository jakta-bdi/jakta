package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.actions.responses.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.AbstractInternalAction

class InternalActionsScope :
    ActionsScope<AgentChange, InternalResponse, InternalRequest, InternalAction, InternalActionScope>() {
    public override fun newAction(name: String, arity: Int, f: InternalActionScope.() -> Unit): InternalAction =
        object : AbstractInternalAction(name, arity) {
            override suspend fun action(request: InternalRequest) {
                InternalActionScope(this, request).f()
            }
        }
}
