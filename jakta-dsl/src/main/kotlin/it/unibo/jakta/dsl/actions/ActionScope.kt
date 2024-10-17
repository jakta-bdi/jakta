package it.unibo.jakta.dsl.actions

import it.unibo.jakta.actions.Action
import it.unibo.jakta.actions.ActionRequest
import it.unibo.jakta.actions.ActionResponse
import it.unibo.jakta.actions.effects.SideEffect

interface ActionScope<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>, A : Action<C, Res, Req>> :
    ActionRequest<C, Res>, Action<C, Res, Req>
