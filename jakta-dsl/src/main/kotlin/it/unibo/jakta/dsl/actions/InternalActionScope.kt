package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.requests.InternalRequest
import it.unibo.jakta.actions.responses.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange

class InternalActionScope(action: InternalAction, request: InternalRequest) :
    ActionScope<AgentChange, InternalResponse, InternalRequest, InternalAction>,
    InternalAction by action,
    InternalRequest by request
