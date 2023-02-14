package io.github.anitvam.agents.bdi.dsl.actions

import io.github.anitvam.agents.bdi.actions.Action
import io.github.anitvam.agents.bdi.actions.ActionRequest
import io.github.anitvam.agents.bdi.actions.ActionResponse
import io.github.anitvam.agents.bdi.actions.effects.SideEffect

@Suppress("unused")
interface ActionScope<C : SideEffect, Res : ActionResponse<C>, Req : ActionRequest<C, Res>, A : Action<C, Res, Req>> :
    ActionRequest<C, Res>, Action<C, Res, Req>
