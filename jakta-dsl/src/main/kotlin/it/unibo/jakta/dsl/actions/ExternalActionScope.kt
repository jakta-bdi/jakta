package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.ExternalAction
import it.unibo.jakta.actions.ExternalRequest
import it.unibo.jakta.actions.ExternalResponse
import it.unibo.jakta.actions.effects.EnvironmentChange

class ExternalActionScope(action: ExternalAction, request: ExternalRequest) :
    ActionScope<EnvironmentChange, ExternalResponse, ExternalRequest, ExternalAction>,
    ExternalAction by action,
    ExternalRequest by request
