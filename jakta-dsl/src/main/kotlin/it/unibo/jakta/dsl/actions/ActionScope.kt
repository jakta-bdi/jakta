package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.ASAction
import it.unibo.jakta.actions.ActionRequest
import it.unibo.jakta.actions.ActionResponse
import it.unibo.jakta.actions.effects.ActionResult

interface ActionScope<C : ActionResult, Res : ActionResponse<C>, Req : ActionRequest<C, Res>, A : ASAction<C, Res, Req>> :
    ActionRequest<C, Res>, ASAction<C, Res, Req>
