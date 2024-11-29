package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.requests.ActionRequest
import it.unibo.jakta.actions.responses.ActionResponse
import it.unibo.jakta.actions.effects.ActionSideEffect

interface ActionScope<C : ActionSideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>, A : ASAction<C, Res, Req>> :
    ActionRequest<C, Res>, ASAction<C, Res, Req>
