package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.effects.EnvironmentChange
import it.unibo.jakta.actions.requests.ExternalRequest

class ExternalActionScope(action: ExternalAction, request: ExternalRequest) :
    ActionScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction>,
    ExternalAction by action,
    ExternalRequest by request
