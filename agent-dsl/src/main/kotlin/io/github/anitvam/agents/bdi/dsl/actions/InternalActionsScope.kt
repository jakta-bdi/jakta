package io.github.anitvam.agents.bdi.dsl.actions

import io.github.anitvam.agents.bdi.actions.InternalAction
import io.github.anitvam.agents.bdi.actions.InternalRequest
import io.github.anitvam.agents.bdi.actions.InternalResponse
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.impl.AbstractInternalAction

class InternalActionsScope :
    ActionsScope<AgentChange, InternalResponse, InternalRequest, InternalAction, InternalActionScope>() {
    override fun newAction(name: String, arity: Int, f: InternalActionScope.() -> Unit): InternalAction =
        object : AbstractInternalAction(name, arity) {
            override fun action(request: InternalRequest) {
                InternalActionScope(this, request).f()
            }
        }
}
