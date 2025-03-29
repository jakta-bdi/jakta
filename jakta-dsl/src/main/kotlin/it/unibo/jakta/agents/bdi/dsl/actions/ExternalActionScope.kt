package it.unibo.jakta.agents.bdi.dsl.actions

import it.unibo.jakta.agents.bdi.actions.ExternalAction
import it.unibo.jakta.agents.bdi.actions.ExternalRequest
import it.unibo.jakta.agents.bdi.actions.ExternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.EnvironmentChange

class ExternalActionScope(
    action: ExternalAction,
    request: ExternalRequest,
) : ActionScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction>,
    ExternalAction by action,
    ExternalRequest by request
