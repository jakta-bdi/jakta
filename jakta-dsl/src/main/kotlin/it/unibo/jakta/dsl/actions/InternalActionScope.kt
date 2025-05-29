package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.effects.AgentChange
import it.unibo.jakta.actions.requests.InternalRequest

class InternalActionScope(action: InternalAction, request: InternalRequest) :
    ActionScope<AgentChange, InternalResponse, InternalRequest, InternalAction>,
    InternalAction by action,
    InternalRequest by request
