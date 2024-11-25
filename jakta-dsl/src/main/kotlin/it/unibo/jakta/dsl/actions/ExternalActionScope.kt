package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.requests.ExternalRequest
import it.unibo.jakta.actions.responses.ExternalResponse
import it.unibo.jakta.actions.effects.EnvironmentChange

class ExternalActionScope(action: ExternalAction, request: ExternalRequest) :
    ActionScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction>,
    ExternalAction by action,
    ExternalRequest by request
