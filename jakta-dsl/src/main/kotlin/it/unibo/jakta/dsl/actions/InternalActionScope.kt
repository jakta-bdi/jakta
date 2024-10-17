package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.InternalAction
import it.unibo.jakta.actions.InternalRequest
import it.unibo.jakta.actions.InternalResponse
import it.unibo.jakta.actions.effects.AgentChange

class InternalActionScope(action: InternalAction, request: InternalRequest) :
    ActionScope<AgentChange, InternalResponse, InternalRequest, InternalAction>,
    InternalAction by action,
    InternalRequest by request
