package it.unibo.jakta.agents.bdi.dsl.actions

import it.unibo.jakta.agents.bdi.actions.InternalAction
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.InternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.AgentChange
import it.unibo.jakta.agents.bdi.actions.impl.AbstractInternalAction

class InternalActionsScope :
    ActionsScope<AgentChange, InternalResponse, InternalRequest, InternalAction, InternalActionScope>() {
    public override fun newAction(name: String, arity: Int, f: InternalActionScope.() -> Unit): InternalAction =
        object : AbstractInternalAction(name, arity) {
            override fun action(request: InternalRequest) {
                InternalActionScope(this, request).f()
            }
        }
}
