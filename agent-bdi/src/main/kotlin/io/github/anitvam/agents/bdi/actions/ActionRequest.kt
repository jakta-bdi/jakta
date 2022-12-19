package io.github.anitvam.agents.bdi.actions

import io.github.anitvam.agents.bdi.AgentContext
import io.github.anitvam.agents.bdi.actions.effects.AgentChange
import io.github.anitvam.agents.bdi.actions.effects.EnvironmentChange
import io.github.anitvam.agents.bdi.actions.effects.SideEffect
import io.github.anitvam.agents.bdi.actions.impl.InternalRequestImpl
import it.unibo.tuprolog.core.Substitution
import it.unibo.tuprolog.core.Term

sealed interface ActionRequest<C : SideEffect, Res : ActionResponse<C>> {
    val arguments: List<Term>
    // val data: Map<String, Any>

    fun reply(substitution: Substitution = Substitution.empty(), effects: Iterable<C>): Res

    fun reply(substitution: Substitution = Substitution.empty(), vararg effects: C): Res
}

interface InternalRequest : ActionRequest<AgentChange, InternalResponse> {
    val agent: AgentContext

    companion object {
        fun of(context: AgentContext, arguments: Iterable<Term>): InternalRequest =
            InternalRequestImpl(context, arguments.toList())

        fun of(context: AgentContext, vararg arguments: Term): InternalRequest =
            of(context, arguments.asList())
    }
}

interface ExternalRequest : ActionRequest<EnvironmentChange, ExternalResponse> {
    // add fields to let the action observe the environment
}
