package io.github.anitvam.agents.bdi.dsl.actions

import io.github.anitvam.agents.bdi.actions.ExternalAction
import io.github.anitvam.agents.bdi.actions.ExternalRequest
import io.github.anitvam.agents.bdi.actions.ExternalResponse
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.impl.AbstractExternalAction

class ExternalActionsScope :
    ActionsScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction, ExternalActionScope>() {
    override fun newAction(name: String, arity: Int, f: ExternalActionScope.() -> Unit): ExternalAction =
        object : AbstractExternalAction(name, arity) {
            override fun action(request: ExternalRequest) {
                ExternalActionScope(this, request).f()
            }
        }
}
