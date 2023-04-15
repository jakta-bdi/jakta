package it.unibo.jakta.agents.bdi.dsl.actions

import it.unibo.jakta.agents.bdi.actions.InternalAction
import it.unibo.jakta.agents.bdi.actions.InternalRequest
import it.unibo.jakta.agents.bdi.actions.InternalResponse
import it.unibo.jakta.agents.bdi.actions.effects.AgentChange

class InternalActionScope(action: InternalAction, request: InternalRequest) :
    ActionScope<AgentChange, InternalResponse, InternalRequest, InternalAction>,
    InternalAction by action,
    InternalRequest by request
